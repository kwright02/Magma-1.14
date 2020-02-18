package org.magmafoundation.magma.util.item;

import com.google.common.base.Preconditions;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MagmaAttributeInstance implements AttributeInstance {

    private final IAttributeInstance handle;
    private final Attribute attribute;

    public MagmaAttributeInstance(IAttributeInstance handle, Attribute attribute) {
        this.handle = handle;
        this.attribute = attribute;
    }

    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public double getBaseValue() {
        return handle.getBaseValue();
    }

    @Override
    public void setBaseValue(double d) {
        handle.setBaseValue(d);
    }

    @Override
    public Collection<AttributeModifier> getModifiers() {
        List<AttributeModifier> result = new ArrayList<AttributeModifier>();
        for (net.minecraft.entity.ai.attributes.AttributeModifier nms : handle.getModifiers()) {
            result.add(convertToBukkit(nms));
        }

        return result;
    }

    @Override
    public void addModifier(AttributeModifier modifier) {
        Preconditions.checkArgument(modifier != null, "modifier");
        handle.applyModifier(convertToNMS(modifier));
    }

    @Override
    public void removeModifier(AttributeModifier modifier) {
        Preconditions.checkArgument(modifier != null, "modifier");
        handle.removeModifier(convertToNMS(modifier));
    }

    @Override
    public double getValue() {
        return handle.getValue();
    }

    @Override
    public double getDefaultValue() {
        return handle.getAttribute().getDefaultValue();
    }

    public static net.minecraft.entity.ai.attributes.AttributeModifier convertToNMS(AttributeModifier bukkit) {
        return new net.minecraft.entity.ai.attributes.AttributeModifier(bukkit.getUniqueId(), bukkit.getName(), bukkit.getAmount(), net.minecraft.entity.ai.attributes.AttributeModifier.Operation.values()[bukkit.getOperation().ordinal()]);
    }

    public static AttributeModifier convertToBukkit(net.minecraft.entity.ai.attributes.AttributeModifier nms) {
        return new AttributeModifier(nms.getID(), nms.getName(), nms.getAmount(), AttributeModifier.Operation.values()[nms.getOperation().ordinal()]);
    }
}
