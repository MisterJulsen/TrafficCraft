package de.mrjulsen.trafficcraft.data.textures.decoder.context;

public interface IDecoderContext {
    Empty EMPTY = new Empty();
    final class Empty implements IDecoderContext {
        private Empty() {}
    }
}
