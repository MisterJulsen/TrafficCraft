package de.mrjulsen.trafficcraft.client.screen;

import java.util.List;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.EventListenerId;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager;
import de.mrjulsen.trafficcraft.client.TrafficLightTextureManager.TrafficLightTextureKey;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.TrafficLightConfig;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.TrafficLightControlSettings;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.TrafficLightGeneralSettings;
import de.mrjulsen.trafficcraft.client.widgets.trafficlight.TrafficLightSignalSettings;
import de.mrjulsen.trafficcraft.network.packets.cts.TrafficLightPacket;
import de.mrjulsen.trafficcraft.registry.ModBlocks;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TrafficLightConfigScreen extends DLWindow {
    
    public static final int WINDOW_WIDTH = 256;
    public static final int WINDOW_HEIGHT = 230;

    private final TrafficLightConfig config;

    private final Component title = TextUtils.translate("gui.trafficcraft.trafficlight.title");

    public TrafficLightConfigScreen(DLWindowManager manager, Level level, BlockPos pos) {
        super(manager);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        windowSpawnPosition.set(WindowPosition.CENTER);

        this.config = new TrafficLightConfig(level, pos);

        DLPanel trafficLightSettings = addComponent(new DLPanel(0, 0, 1, 1));        
        FlowLayout settingsLayout = new FlowLayout();
        settingsLayout.fillCrossAxis.set(true);
        settingsLayout.flowDirection.set(Direction.HORIZONTAL);
        settingsLayout.horizontalGap.set(5);
        settingsLayout.wrap.set(false);

        DLPanel optionsPanel = new DLPanel(0, 0, 1, 1);
        optionsPanel.layoutContraint.set(FlowLayout.FlowConstraint.FILL);

        TrafficLightPanel trafficLightPanel = new TrafficLightPanel(0, 0, config, optionsPanel);
        trafficLightPanel.addEventListener(DLGuiStandardEvents.ComponentPosAndSizeChanged.class, (s, e) -> {
            if (e.heightChanged()) {
                trafficLightSettings.setHeight(e.newHeight());
            }
            return false;
        });

        trafficLightSettings.addComponent(trafficLightPanel);
        trafficLightSettings.addComponent(optionsPanel);
        trafficLightSettings.setHeight(trafficLightPanel.height());
        trafficLightSettings.layout.set(settingsLayout);

        addComponent(new TrafficLightControlSettings(0, 0, 1, 100, config));

        FlowLayout layout = new FlowLayout();
        layout.fillCrossAxis.set(true);
        layout.flowDirection.set(Direction.VERTICAL);
        layout.verticalGap.set(5);
        layout.wrap.set(false);
        layout.padding.set(new Padding(20, 0, 0, 0));
        addEventListener(DLGuiStandardEvents.ComponentLayoutUpdatedEvent.class, (s, e) -> {
            setHeight(20 + e.layoutResult().contentHeight());
            return false;
        });
        this.layout.set(layout);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 0, title, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.CENTER, true);
    }

    @Override
    public void close() throws Exception {        
        ModNetworkManager.UPDATE_TRAFFIC_LIGHT_PACKET.send(NetworkDirection.toServer(), new TrafficLightPacket(
            config.blockPos,
            config.enabledColors,
            config.type,
            config.model,
            config.icon,
            config.controlType,
            config.colors,
            config.phaseId,
            config.scheduleEnabled
        ));  
    }

    private static class TrafficLightPanel extends DLGuiComponent {

        private static final int SCALE = 6;
        private static final int OFFSET = 1;
        private static final float TRAFFIC_LIGHT_WIDTH = 8;
        private static final float TRAFFIC_LIGHT_HEIGHT = 16.5f;
        private static final int TRAFFIC_LIGHT_SIGNAL_SIZE = 4;

        private final DLPanel optionsPanel;
        private final TrafficLightConfig config;
        private final EventListenerId eventId;
        
        private static final Component textEditTrafficLight = TextUtils.translate("gui.trafficcraft.trafficlight.edit_traffic_light");

        public TrafficLightPanel(int x, int y, TrafficLightConfig config, DLPanel optionsPanel) {
            super(x, y, 1, 1);
            setSize((TRAFFIC_LIGHT_WIDTH + OFFSET * 2) * SCALE, (TRAFFIC_LIGHT_HEIGHT + OFFSET * 2) * SCALE);
            this.config = config;
            this.optionsPanel = optionsPanel;

            tooltip.set(new DLTooltip(List.of(textEditTrafficLight), 200));

            addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                optionsPanel.clearComponents();
                TrafficLightGeneralSettings settings = optionsPanel.addComponent(new TrafficLightGeneralSettings(0, 0, optionsPanel.width() - width() - 5, 100, config));
                settings.setY(optionsPanel.height() / 2 - settings.height() / 2);
                return false;
            });

            eventId = config.addEventListener(TrafficLightConfig.UpdateEvent.class, (s, e) -> {
                update();
                return false;
            });
            update();
        }

        @Override
        public void close() throws Exception {
            config.removeEventListener(TrafficLightConfig.UpdateEvent.class, eventId);
        }
        
        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            BlockState state = ModBlocks.TRAFFIC_LIGHT.get().defaultBlockState();
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0, 0, 200);

            GuiUtils.renderBlockState(graphics, (int)(width() / 2 - TRAFFIC_LIGHT_WIDTH * SCALE), SCALE, SCALE, state.setValue(TrafficLightBlock.MODEL, config.model), RenderType.solid(), LightTexture.FULL_BRIGHT);
                            
            if (isSelected()) {
                GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0x40FFFFFF), DLColor.WHITE);
            }

            graphics.poseStack().popPose();
        }

        public void update() {
            clearComponents();
            float x = 2 + OFFSET, y = 1.5f + OFFSET;
            for (byte i = 0; i < config.model.getLightsCount(); i++) {
                final byte k = i;
                TrafficLightSignalButton signal = new TrafficLightSignalButton((int)(x * SCALE), (int)((y + (i * 5)) * SCALE), SCALE * TRAFFIC_LIGHT_SIGNAL_SIZE, SCALE * TRAFFIC_LIGHT_SIGNAL_SIZE, config, i);
                signal.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                    optionsPanel.clearComponents();
                    TrafficLightSignalSettings settings = optionsPanel.addComponent(new TrafficLightSignalSettings(0, 0, optionsPanel.width() - width() - 5, 100, k, config));                    
                    settings.setY((y + (k * 5) + TRAFFIC_LIGHT_SIGNAL_SIZE / 2) * SCALE - settings.height() / 2);
                    return false;
                });
                addComponent(signal);
            }
        }
    }    

    private static class TrafficLightSignalButton extends DLGuiComponent {

        private final TrafficLightConfig config;
        private final byte signalId;
        
        private final String keyAreaTrafficLightSignal = "gui.trafficcraft.trafficlight.edit_signal_";

        public TrafficLightSignalButton(int x, int y, int w, int h, TrafficLightConfig config, byte signalId) {
            super(x, y, w, h);
            this.config = config;
            this.signalId = signalId;
            tooltip.set(new DLTooltip(List.of(TextUtils.translate(keyAreaTrafficLightSignal + signalId)), 200));
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            graphics.poseStack().pushPose();
            graphics.poseStack().translate(0, 0, 200);
            GuiUtils.drawTexture(TrafficLightTextureManager.getResourceLocation(new TrafficLightTextureKey(config.icon, config.colors[signalId])), graphics, 0, 0, width(), height(), 0, 0, 16, 16, TextureFillMode.STRETCH, 16, 16);
            if (isSelected()) {
                GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0x40FFFFFF), DLColor.WHITE);
            }
            graphics.poseStack().popPose();
        }
        
    }
}
