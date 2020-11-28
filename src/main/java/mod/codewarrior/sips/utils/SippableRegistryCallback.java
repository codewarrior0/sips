package mod.codewarrior.sips.utils;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface SippableRegistryCallback {

    Event<SippableRegistryCallback> EVENT = EventFactory.createArrayBacked(SippableRegistryCallback.class, (listeners) -> () -> {
        for (SippableRegistryCallback listener : listeners) {
           listener.registerSippable();
        }
    });

    void registerSippable();
}
