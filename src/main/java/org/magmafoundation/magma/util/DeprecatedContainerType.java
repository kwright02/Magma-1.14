package org.magmafoundation.magma.util;

import org.apache.commons.lang3.Validate;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.magmafoundation.magma.util.item.MagmaPersistentDataContainer;

public final class DeprecatedContainerTagType<Z> implements PersistentDataType<PersistentDataContainer, Z> {

    private final ItemTagType<CustomItemTagContainer, Z> deprecated;

    DeprecatedContainerTagType(ItemTagType<CustomItemTagContainer, Z> deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public Class<Z> getComplexType() {
        return deprecated.getComplexType();
    }

    @Override
    public PersistentDataContainer toPrimitive(Z complex, PersistentDataAdapterContext context) {
        CustomItemTagContainer deprecated = this.deprecated.toPrimitive(complex, new DeprecatedItemAdapterContext(context));
        Validate.isInstanceOf(DeprecatedCustomTagContainer.class, deprecated, "Could not wrap deprecated API due to foreign CustomItemTagContainer implementation %s", deprecated.getClass().getSimpleName());

        DeprecatedCustomTagContainer tagContainer = (DeprecatedCustomTagContainer) deprecated;
        PersistentDataContainer wrapped = tagContainer.getWrapped();
        Validate.isInstanceOf(MagmaPersistentDataContainer.class, wrapped, "Could not wrap deprecated API due to wrong deprecation wrapper %s", deprecated.getClass().getSimpleName());

        MagmaPersistentDataContainer craftTagContainer = (MagmaPersistentDataContainer) wrapped;
        return new MagmaPersistentDataContainer(craftTagContainer.getRaw(), craftTagContainer.getDataTagTypeRegistry());
    }

    @Override
    public Z fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
        Validate.isInstanceOf(MagmaPersistentDataContainer.class, primitive, "Could not wrap deprecated API due to foreign PersistentMetadataContainer implementation %s", primitive.getClass().getSimpleName());

        return this.deprecated.fromPrimitive(new DeprecatedCustomTagContainer(primitive), new DeprecatedItemAdapterContext(context));
    }
}