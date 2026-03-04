package de.mrjulsen.trafficcraft.client.screen;

import java.util.Arrays;
import java.util.stream.IntStream;

import de.mrjulsen.mcdragonlib.block.DLWritableSignBlockEntity;
import de.mrjulsen.mcdragonlib.client.gui.builtin.WritableSignScreen;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.trafficcraft.block.TownSignBlock;
import de.mrjulsen.trafficcraft.block.data.TownSignVariant;
import de.mrjulsen.trafficcraft.block.entity.TownSignBlockEntity;
import de.mrjulsen.trafficcraft.network.packets.cts.TownSignPacket;
import de.mrjulsen.trafficcraft.registry.ModNetworkManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class TownSignScreen extends WritableSignScreen {
    
    private Component textVariant = TextUtils.translate("gui.trafficcraft.townsignvariant");
    private TownSignVariant variant;
    private TownSignBlock.ETownSignSide side;

    public TownSignScreen(DLWritableSignBlockEntity pSign, TownSignBlock.ETownSignSide side) {
        this(
            pSign,
            getConfig(pSign, side),
            getState(pSign, side, pSign.getBlockState().getValue(TownSignBlock.VARIANT)),
            getMessages(pSign, getConfig(pSign, side), side)
        );
        this.variant = pSign.getBlockState().getValue(TownSignBlock.VARIANT);
        this.side = side;
    }

    protected TownSignScreen(DLWritableSignBlockEntity pSign, WritableSignConfig config, BlockState state, ConfiguredLine[] messages) {
        super(pSign, config, state, messages);
    }

    protected static WritableSignConfig getConfig(DLWritableSignBlockEntity pSign, TownSignBlock.ETownSignSide side) {
        if (pSign instanceof TownSignBlockEntity blockEntity) {
            switch (side) {
                case BACK:
                    return blockEntity.getBackRenderConfig();
                default:
                    break;
            }
        }    
        return pSign.getRenderConfig();         
    }

    protected static BlockState getState(DLWritableSignBlockEntity pSign, TownSignBlock.ETownSignSide side, TownSignVariant variant) {
        switch (side) {
            case BACK:
                return pSign.getBlockState().getBlock().defaultBlockState().setValue(TownSignBlock.VARIANT, TownSignVariant.BACK);
            default:
            case FRONT:
                return pSign.getBlockState().getBlock().defaultBlockState().setValue(TownSignBlock.VARIANT, TownSignVariant.FRONT);
        }
    }

    @Override
    protected void init() {
        
        this.btnDone = Button.builder(CommonComponents.GUI_DONE, (btn) -> {
            this.onDone();
        }).bounds(this.width / 2 - 100, this.height / 4 + 145, 200, 20).build();
        addRenderableWidget(btnDone);

        CycleButton<TownSignVariant> sideBtn = CycleButton.<TownSignVariant>builder(t -> t.getValueTranslation()).withValues(TownSignVariant.values()).create(this.width / 2 - 100, this.height / 4 + 120, 200, 20, textVariant, (a, b) -> {
            this.variant = b;  
        });
        addRenderableWidget(sideBtn);

        this.signTextField = new TextFieldHelper(() -> {
            return this.messages[this.selectedLine].text;
        }, (text) -> {
            if (this.sign instanceof TownSignBlockEntity blockEntity) {
                this.messages[this.selectedLine].text = text;
                switch (side) {
                    case BACK:
                        blockEntity.setBackText(text, selectedLine);
                        return;
                    default:
                        break;
                }
            }
            this.sign.setText(text, selectedLine);
        }, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (text) -> {
            return text == null || this.minecraft.font.width(text) <= config.lineData()[this.selectedLine].maxLineWidth() * config.scale();
        });
    }

    protected static ConfiguredLine[] getMessages(DLWritableSignBlockEntity pSign, WritableSignConfig config, TownSignBlock.ETownSignSide side) {
        if (pSign instanceof TownSignBlockEntity blockEntity) {
            switch (side) {
                case BACK:
                    return IntStream.range(0, config.lineData().length).mapToObj((i) -> {
                        return new ConfiguredLine(blockEntity.getBackText(i), config.lineData()[i]);
                    }).toArray((length) -> {
                        return new ConfiguredLine[length];
                    });
                default:
                    break;
            }
        }

        return IntStream.range(0, config.lineData().length).mapToObj((i) -> {
            return new ConfiguredLine(pSign.getText(i), config.lineData()[i]);
        }).toArray((length) -> {
            return new ConfiguredLine[length];
        });
    }

    @Override
    public void removed() {
        ModNetworkManager.UPDATE_TOWN_SIGN.send(NetworkDirection.toServer(), new TownSignPacket(this.sign.getBlockPos(), Arrays.stream(messages).map(x -> x.text).toArray(String[]::new), variant, side));
    }

    @Override
    protected void onDone() {
        ModNetworkManager.UPDATE_TOWN_SIGN.send(NetworkDirection.toServer(), new TownSignPacket(this.sign.getBlockPos(), Arrays.stream(messages).map(x -> x.text).toArray(String[]::new), variant, side));
        this.minecraft.setScreen(null);
    }
    
}