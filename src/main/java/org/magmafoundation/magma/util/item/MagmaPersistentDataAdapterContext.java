package org.magmafoundation.magma.util.item;

import org.bukkit.persistence.PersistentDataAdapterContext;

public class MagmaPersistentDataAdapterContext implements PersistentDataAdapterContext {

    private final MagmaPersistentDataTypeRegistry registry;

    public MagmaPersistentDataAdapterContext(MagmaPersistentDataTypeRegistry registry) {
        this.registry = registry;
    }

    /**
     * Creates a new and empty tag container instance
     *
     * @return the fresh container instance
     */
    @Override
    public MagmaPersistentDataContainer newPersistentDataContainer() {
        return new MagmaPersistentDataContainer(this.registry);
    }
}
