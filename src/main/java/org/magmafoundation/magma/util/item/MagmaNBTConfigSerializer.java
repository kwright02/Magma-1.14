package org.magmafoundation.magma.util.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MagmaNBTConfigSerializer {

    private static final Pattern ARRAY = Pattern.compile("^\\[.*]");
    private static final Pattern INTEGER = Pattern.compile("[-+]?(?:0|[1-9][0-9]*)?i", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOUBLE = Pattern.compile("[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d", Pattern.CASE_INSENSITIVE);
    private static final JsonToNBT MOJANGSON_PARSER = new JsonToNBT(new StringReader(""));

    public static Object serialize(INBT base) {
        if (base instanceof CompoundNBT) {
            Map<String, Object> innerMap = new HashMap<>();
            for (String key : ((CompoundNBT) base).keySet()) {
                innerMap.put(key, serialize(((CompoundNBT) base).get(key)));
            }

            return innerMap;
        } else if (base instanceof ListNBT) {
            List<Object> baseList = new ArrayList<>();
            for (int i = 0; i < ((ListNBT) base).size(); i++) {
                baseList.add(serialize((INBT) ((ListNBT) base).get(i)));
            }

            return baseList;
        } else if (base instanceof StringNBT) {
            return base.getString();
        } else if (base instanceof IntNBT) { // No need to check for doubles, those are covered by the double itself
            return base.toString() + "i";
        }

        return base.toString();
    }

    public static INBT deserialize(Object object) {
        if (object instanceof Map) {
            CompoundNBT compound = new CompoundNBT();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) object).entrySet()) {
                compound.put(entry.getKey(), deserialize(entry.getValue()));
            }

            return compound;
        } else if (object instanceof List) {
            List<Object> list = (List<Object>) object;
            if (list.isEmpty()) {
                return new ListNBT(); // Default
            }

            ListNBT tagList = new ListNBT();
            for (Object tag : list) {
                tagList.add(deserialize(tag));
            }

            return tagList;
        } else if (object instanceof String) {
            String string = (String) object;

            if (ARRAY.matcher(string).matches()) {
                try {
                    return new JsonToNBT(new StringReader(string)).readValue();
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException("Could not deserialize found list ", e);
                }
            } else if (INTEGER.matcher(string).matches()) { //Read integers on our own
                return new IntNBT(Integer.parseInt(string.substring(0, string.length() - 1)));
            } else if (DOUBLE.matcher(string).matches()) {
                return new DoubleNBT(Double.parseDouble(string.substring(0, string.length() - 1)));
            } else {
                INBT inbt = null;
                try {
                    inbt = MOJANGSON_PARSER.readStruct().get(string);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

                if (inbt instanceof IntNBT) { // If this returns an integer, it did not use our method from above
                    return new StringNBT(inbt.getString()); // It then is a string that was falsely read as an int
                } else if (inbt instanceof DoubleNBT) {
                    return new StringNBT(String.valueOf(((DoubleNBT) inbt).getDouble())); // Doubles add "d" at the end
                } else {
                    return inbt;
                }
            }
        }

        throw new RuntimeException("Could not deserialize INBT");
    }
    
}
