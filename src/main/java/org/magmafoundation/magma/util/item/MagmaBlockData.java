package org.magmafoundation.magma.util.item;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.INameable;
import net.minecraft.util.IStringSerializable;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.magmafoundation.magma.util.MagmaUnsafeValues;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MagmaBlockData implements BlockData {

    private BlockState state;
    private Map<BlockStateMatcher, Comparable<?>> parsedStates;
    private Material material;

    protected MagmaBlockData() {
        throw new AssertionError("Template Constructor");
    }

    protected MagmaBlockData(BlockState state, Material material) {
        this.state = state;
        this.material = material;
    }

    @Override
    public org.bukkit.Material getMaterial() {
        return MagmaUnsafeValues.getInstance().getMaterial(material.toString(), 0);
    }

    public BlockState getState() {
        return state;
    }

    /**
     * Get a given EnumProperty's value as its Bukkit counterpart.
     *
     * @param nms    the NMS state to convert
     * @param bukkit the Bukkit class
     * @param <B>    the type
     * @return the matching Bukkit type
     */
    protected <B extends Enum<B>> B get(EnumProperty<?> nms, Class<B> bukkit) {
        return toBukkit(state.get(nms), bukkit);
    }

    /**
     * Convert all values from the given EnumProperty to their appropriate
     * Bukkit counterpart.
     *
     * @param nms    the NMS state to get values from
     * @param bukkit the bukkit class to convert the values to
     * @param <B>    the bukkit class type
     * @return an immutable Set of values in their appropriate Bukkit type
     */
    @SuppressWarnings("unchecked")
    protected <B extends Enum<B>> Set<B> getValues(EnumProperty<?> nms, Class<B> bukkit) {
        ImmutableSet.Builder<B> values = ImmutableSet.builder();

        for (Enum<?> e : nms.getAllowedValues()) {
            values.add(toBukkit(e, bukkit));
        }

        return values.build();
    }

    /**
     * Set a given {@link EnumProperty} with the matching enum from Bukkit.
     *
     * @param nms    the NMS EnumProperty to set
     * @param bukkit the matching Bukkit Enum
     * @param <B>    the Bukkit type
     * @param <N>    the NMS type
     */
    protected <B extends Enum<B>, N extends Enum<N> & IStringSerializable> void set(EnumProperty<N> nms, Enum<B> bukkit) {
        this.parsedStates = null;
        this.state = this.state.getExtendedState(nms, toNMS(nms.getAllowedValues(), bukkit));
    }

    @Override
    public BlockData merge(BlockData data) {
        MagmaBlockData craft = (MagmaBlockData) data;
        Preconditions.checkArgument(craft.parsedStates != null, "Data not created via string parsing");
        Preconditions.checkArgument(this.state.getBlock() == craft.state.getBlock(), "States have different types (got %s, expected %s)", data, this);

        MagmaBlockData clone = (MagmaBlockData) this.clone();
        clone.parsedStates = null;

        for (BlockStateMatcher parsed : craft.parsedStates.keySet()) {
            clone.state = clone.state.getExtendedState(parsed, craft.state.get(parsed.));
        }

        return clone;
    }

    @Override
    public boolean matches(BlockData data) {
        if (data == null) {
            return false;
        }
        if (!(data instanceof MagmaBlockData)) {
            return false;
        }

        MagmaBlockData craft = (MagmaBlockData) data;
        if (this.state.getBlock() != craft.state.getBlock()) {
            return false;
        }

        // Fastpath an exact match
        boolean exactMatch = this.equals(data);

        // If that failed, do a merge and check
        if (!exactMatch && craft.parsedStates != null) {
            return this.merge(data).equals(this);
        }

        return exactMatch;
    }

    private static final Map<Class, BiMap<Enum<?>, Enum<?>>> classMappings = new HashMap<>();

    /**
     * Convert an NMS Enum (usually a EnumProperty) to its appropriate Bukkit
     * enum from the given class.
     *
     * @throws IllegalStateException if the Enum could not be converted
     */
    @SuppressWarnings("unchecked")
    private static <B extends Enum<B>> B toBukkit(Enum<?> nms, Class<B> bukkit) {
        Enum<?> converted;
        BiMap<Enum<?>, Enum<?>> nmsToBukkit = classMappings.get(nms.getClass());

        if (nmsToBukkit != null) {
            converted = nmsToBukkit.get(nms);
            if (converted != null) {
                return (B) converted;
            }
        }

        if (nms instanceof EnumDirection) {
            converted = CraftBlock.notchToBlockFace((EnumDirection) nms);
        } else {
            converted = bukkit.getEnumConstants()[nms.ordinal()];
        }

        Preconditions.checkState(converted != null, "Could not convert enum %s->%s", nms, bukkit);

        if (nmsToBukkit == null) {
            nmsToBukkit = HashBiMap.create();
            classMappings.put(nms.getClass(), nmsToBukkit);
        }

        nmsToBukkit.put(nms, converted);

        return (B) converted;
    }

    /**
     * Convert a given Bukkit enum to its matching NMS enum type.
     *
     * @param bukkit the Bukkit enum to convert
     * @param nms    the NMS class
     * @return the matching NMS type
     * @throws IllegalStateException if the Enum could not be converted
     */
    @SuppressWarnings("unchecked")
    private static <N extends Enum<N> & INamable> N toNMS(Enum<?> bukkit, Class<N> nms) {
        Enum<?> converted;
        BiMap<Enum<?>, Enum<?>> nmsToBukkit = classMappings.get(nms);

        if (nmsToBukkit != null) {
            converted = nmsToBukkit.inverse().get(bukkit);
            if (converted != null) {
                return (N) converted;
            }
        }

        if (bukkit instanceof BlockFace) {
            converted = CraftBlock.blockFaceToNotch((BlockFace) bukkit);
        } else {
            converted = nms.getEnumConstants()[bukkit.ordinal()];
        }

        Preconditions.checkState(converted != null, "Could not convert enum %s->%s", nms, bukkit);

        if (nmsToBukkit == null) {
            nmsToBukkit = HashBiMap.create();
            classMappings.put(nms, nmsToBukkit);
        }

        nmsToBukkit.put(converted, bukkit);

        return (N) converted;
    }

    /**
     * Get the current value of a given state.
     *
     * @param ibs the state to check
     * @param <T> the type
     * @return the current value of the given state
     */
    protected <T extends Comparable<T>> T get(BlockStateMatcher<T> ibs) {
        // Straight integer or boolean getter
        return this.state.get(ibs);
    }

    /**
     * Set the specified state's value.
     *
     * @param ibs the state to set
     * @param v   the new value
     * @param <T> the state's type
     * @param <V> the value's type. Must match the state's type.
     */
    public <T extends Comparable<T>, V extends T> void set(BlockStateMatcher<T> ibs, V v) {
        // Straight integer or boolean setter
        this.parsedStates = null;
        this.state = this.state.set(ibs, v);
    }

    @Override
    public String getAsString() {
        return toString(((BlockDataAbstract) state).getStateMap());
    }

    @Override
    public String getAsString(boolean hideUnspecified) {
        return (hideUnspecified && parsedStates != null) ? toString(parsedStates) : getAsString();
    }

    @Override
    public BlockData clone() {
        try {
            return (BlockData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Clone not supported", ex);
        }
    }

    @Override
    public String toString() {
        return "MagmaBlockData{" + getAsString() + "}";
    }

    // Mimicked from BlockDataAbstract#toString()
    public String toString(Map<BlockStateMatcher<?>, Comparable<?>> states) {
        StringBuilder stateString = new StringBuilder(IRegistry.BLOCK.getKey(state.getBlock()).toString());

        if (!states.isEmpty()) {
            stateString.append('[');
            stateString.append(states.entrySet().stream().map(BlockDataAbstract.STATE_TO_VALUE).collect(Collectors.joining(",")));
            stateString.append(']');
        }

        return stateString.toString();
    }

    public NBTTagCompound toStates() {
        NBTTagCompound compound = new NBTTagCompound();

        for (Map.Entry<BlockStateMatcher<?>, Comparable<?>> entry : state.getStateMap().entrySet()) {
            BlockStateMatcher BlockStateMatcher = (BlockStateMatcher) entry.getKey();

            compound.setString(BlockStateMatcher.a(), BlockStateMatcher.a(entry.getValue()));
        }

        return compound;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MagmaBlockData && state.equals(((MagmaBlockData) obj).state);
    }

    @Override
    public int hashCode() {
        return state.hashCode();
    }

    protected static BlockStateBoolean getBoolean(String name) {
        throw new AssertionError("Template Method");
    }

    protected static BlockStateBoolean getBoolean(String name, boolean optional) {
        throw new AssertionError("Template Method");
    }

    protected static EnumProperty<?> getEnum(String name) {
        throw new AssertionError("Template Method");
    }

    protected static BlockStateInteger getInteger(String name) {
        throw new AssertionError("Template Method");
    }

    protected static BlockStateBoolean getBoolean(Class<? extends Block> block, String name) {
        return (BlockStateBoolean) getState(block, name, false);
    }

    protected static BlockStateBoolean getBoolean(Class<? extends Block> block, String name, boolean optional) {
        return (BlockStateBoolean) getState(block, name, optional);
    }

    protected static EnumProperty<?> getEnum(Class<? extends Block> block, String name) {
        return (EnumProperty<?>) getState(block, name, false);
    }

    protected static BlockStateInteger getInteger(Class<? extends Block> block, String name) {
        return (BlockStateInteger) getState(block, name, false);
    }

    /**
     * Get a specified {@link BlockStateMatcher} from a given block's class with a
     * given name
     *
     * @param block    the class to retrieve the state from
     * @param name     the name of the state to retrieve
     * @param optional if the state can be null
     * @return the specified state or null
     * @throws IllegalStateException if the state is null and {@code optional}
     *                               is false.
     */
    private static BlockStateMatcher<?> getState(Class<? extends Block> block, String name, boolean optional) {
        BlockStateMatcher<?> state = null;

        for (Block instance : IRegistry.BLOCK) {
            if (instance.getClass() == block) {
                if (state == null) {
                    state = instance.getStates().a(name);
                } else {
                    BlockStateMatcher<?> newState = instance.getStates().a(name);

                    Preconditions.checkState(state == newState, "State mistmatch %s,%s", state, newState);
                }
            }
        }

        Preconditions.checkState(optional || state != null, "Null state for %s,%s", block, name);

        return state;
    }

    /**
     * Get the minimum value allowed by the BlockStateInteger.
     *
     * @param state the state to check
     * @return the minimum value allowed
     */
    protected static int getMin(BlockStateInteger state) {
        return state.min;
    }

    /**
     * Get the maximum value allowed by the BlockStateInteger.
     *
     * @param state the state to check
     * @return the maximum value allowed
     */
    protected static int getMax(BlockStateInteger state) {
        return state.max;
    }

    //
    private static final Map<Class<? extends Block>, Function<BlockPattern, MagmaBlockData>> MAP = new HashMap<>();

//    static {
//        //<editor-fold desc="MagmaBlockData Registration" defaultstate="collapsed">
//        register(net.minecraft.block.BlockAnvil.class, org.bukkit.craftbukkit.block.impl.CraftAnvil::new);
//        register(net.minecraft.block.BlockBamboo.class, org.bukkit.craftbukkit.block.impl.CraftBamboo::new);
//        register(net.minecraft.block.BlockBanner.class, org.bukkit.craftbukkit.block.impl.CraftBanner::new);
//        register(net.minecraft.block.BlockBannerWall.class, org.bukkit.craftbukkit.block.impl.CraftBannerWall::new);
//        register(net.minecraft.block.BlockBarrel.class, org.bukkit.craftbukkit.block.impl.CraftBarrel::new);
//        register(net.minecraft.block.BlockBed.class, org.bukkit.craftbukkit.block.impl.CraftBed::new);
//        register(net.minecraft.block.BlockBeehive.class, org.bukkit.craftbukkit.block.impl.CraftBeehive::new);
//        register(net.minecraft.block.BlockBeetroot.class, org.bukkit.craftbukkit.block.impl.CraftBeetroot::new);
//        register(net.minecraft.block.BlockBell.class, org.bukkit.craftbukkit.block.impl.CraftBell::new);
//        register(net.minecraft.block.BlockBlastFurnace.class, org.bukkit.craftbukkit.block.impl.CraftBlastFurnace::new);
//        register(net.minecraft.block.BlockBrewingStand.class, org.bukkit.craftbukkit.block.impl.CraftBrewingStand::new);
//        register(net.minecraft.block.BlockBubbleColumn.class, org.bukkit.craftbukkit.block.impl.CraftBubbleColumn::new);
//        register(net.minecraft.block.BlockCactus.class, org.bukkit.craftbukkit.block.impl.CraftCactus::new);
//        register(net.minecraft.block.BlockCake.class, org.bukkit.craftbukkit.block.impl.CraftCake::new);
//        register(net.minecraft.block.BlockCampfire.class, org.bukkit.craftbukkit.block.impl.CraftCampfire::new);
//        register(net.minecraft.block.BlockCarrots.class, org.bukkit.craftbukkit.block.impl.CraftCarrots::new);
//        register(net.minecraft.block.BlockCauldron.class, org.bukkit.craftbukkit.block.impl.CraftCauldron::new);
//        register(net.minecraft.block.BlockChest.class, org.bukkit.craftbukkit.block.impl.CraftChest::new);
//        register(net.minecraft.block.BlockChestTrapped.class, org.bukkit.craftbukkit.block.impl.CraftChestTrapped::new);
//        register(net.minecraft.block.BlockChorusFlower.class, org.bukkit.craftbukkit.block.impl.CraftChorusFlower::new);
//        register(net.minecraft.block.BlockChorusFruit.class, org.bukkit.craftbukkit.block.impl.CraftChorusFruit::new);
//        register(net.minecraft.block.BlockCobbleWall.class, org.bukkit.craftbukkit.block.impl.CraftCobbleWall::new);
//        register(net.minecraft.block.BlockCocoa.class, org.bukkit.craftbukkit.block.impl.CraftCocoa::new);
//        register(net.minecraft.block.BlockCommand.class, org.bukkit.craftbukkit.block.impl.CraftCommand::new);
//        register(net.minecraft.block.BlockComposter.class, org.bukkit.craftbukkit.block.impl.CraftComposter::new);
//        register(net.minecraft.block.BlockConduit.class, org.bukkit.craftbukkit.block.impl.CraftConduit::new);
//        register(net.minecraft.block.BlockCoralDead.class, org.bukkit.craftbukkit.block.impl.CraftCoralDead::new);
//        register(net.minecraft.block.BlockCoralFan.class, org.bukkit.craftbukkit.block.impl.CraftCoralFan::new);
//        register(net.minecraft.block.BlockCoralFanAbstract.class, org.bukkit.craftbukkit.block.impl.CraftCoralFanAbstract::new);
//        register(net.minecraft.block.BlockCoralFanWall.class, org.bukkit.craftbukkit.block.impl.CraftCoralFanWall::new);
//        register(net.minecraft.block.BlockCoralFanWallAbstract.class, org.bukkit.craftbukkit.block.impl.CraftCoralFanWallAbstract::new);
//        register(net.minecraft.block.BlockCoralPlant.class, org.bukkit.craftbukkit.block.impl.CraftCoralPlant::new);
//        register(net.minecraft.block.BlockCrops.class, org.bukkit.craftbukkit.block.impl.CraftCrops::new);
//        register(net.minecraft.block.BlockDaylightDetector.class, org.bukkit.craftbukkit.block.impl.CraftDaylightDetector::new);
//        register(net.minecraft.block.BlockDirtSnow.class, org.bukkit.craftbukkit.block.impl.CraftDirtSnow::new);
//        register(net.minecraft.block.BlockDispenser.class, org.bukkit.craftbukkit.block.impl.CraftDispenser::new);
//        register(net.minecraft.block.BlockDoor.class, org.bukkit.craftbukkit.block.impl.CraftDoor::new);
//        register(net.minecraft.block.BlockDropper.class, org.bukkit.craftbukkit.block.impl.CraftDropper::new);
//        register(net.minecraft.block.BlockEndRod.class, org.bukkit.craftbukkit.block.impl.CraftEndRod::new);
//        register(net.minecraft.block.BlockEnderChest.class, org.bukkit.craftbukkit.block.impl.CraftEnderChest::new);
//        register(net.minecraft.block.BlockEnderPortalFrame.class, org.bukkit.craftbukkit.block.impl.CraftEnderPortalFrame::new);
//        register(net.minecraft.block.BlockFence.class, org.bukkit.craftbukkit.block.impl.CraftFence::new);
//        register(net.minecraft.block.BlockFenceGate.class, org.bukkit.craftbukkit.block.impl.CraftFenceGate::new);
//        register(net.minecraft.block.BlockFire.class, org.bukkit.craftbukkit.block.impl.CraftFire::new);
//        register(net.minecraft.block.BlockFloorSign.class, org.bukkit.craftbukkit.block.impl.CraftFloorSign::new);
//        register(net.minecraft.block.BlockFluids.class, org.bukkit.craftbukkit.block.impl.CraftFluids::new);
//        register(net.minecraft.block.BlockFurnaceFurace.class, org.bukkit.craftbukkit.block.impl.CraftFurnaceFurace::new);
//        register(net.minecraft.block.BlockGlazedTerracotta.class, org.bukkit.craftbukkit.block.impl.CraftGlazedTerracotta::new);
//        register(net.minecraft.block.BlockGrass.class, org.bukkit.craftbukkit.block.impl.CraftGrass::new);
//        register(net.minecraft.block.BlockGrindstone.class, org.bukkit.craftbukkit.block.impl.CraftGrindstone::new);
//        register(net.minecraft.block.BlockHay.class, org.bukkit.craftbukkit.block.impl.CraftHay::new);
//        register(net.minecraft.block.BlockHopper.class, org.bukkit.craftbukkit.block.impl.CraftHopper::new);
//        register(net.minecraft.block.BlockHugeMushroom.class, org.bukkit.craftbukkit.block.impl.CraftHugeMushroom::new);
//        register(net.minecraft.block.BlockIceFrost.class, org.bukkit.craftbukkit.block.impl.CraftIceFrost::new);
//        register(net.minecraft.block.BlockIronBars.class, org.bukkit.craftbukkit.block.impl.CraftIronBars::new);
//        register(net.minecraft.block.BlockJigsaw.class, org.bukkit.craftbukkit.block.impl.CraftJigsaw::new);
//        register(net.minecraft.block.BlockJukeBox.class, org.bukkit.craftbukkit.block.impl.CraftJukeBox::new);
//        register(net.minecraft.block.BlockKelp.class, org.bukkit.craftbukkit.block.impl.CraftKelp::new);
//        register(net.minecraft.block.BlockLadder.class, org.bukkit.craftbukkit.block.impl.CraftLadder::new);
//        register(net.minecraft.block.BlockLantern.class, org.bukkit.craftbukkit.block.impl.CraftLantern::new);
//        register(net.minecraft.block.BlockLeaves.class, org.bukkit.craftbukkit.block.impl.CraftLeaves::new);
//        register(net.minecraft.block.BlockLectern.class, org.bukkit.craftbukkit.block.impl.CraftLectern::new);
//        register(net.minecraft.block.BlockLever.class, org.bukkit.craftbukkit.block.impl.CraftLever::new);
//        register(net.minecraft.block.BlockLogAbstract.class, org.bukkit.craftbukkit.block.impl.CraftLogAbstract::new);
//        register(net.minecraft.block.BlockLoom.class, org.bukkit.craftbukkit.block.impl.CraftLoom::new);
//        register(net.minecraft.block.BlockMinecartDetector.class, org.bukkit.craftbukkit.block.impl.CraftMinecartDetector::new);
//        register(net.minecraft.block.BlockMinecartTrack.class, org.bukkit.craftbukkit.block.impl.CraftMinecartTrack::new);
//        register(net.minecraft.block.BlockMycel.class, org.bukkit.craftbukkit.block.impl.CraftMycel::new);
//        register(net.minecraft.block.BlockNetherWart.class, org.bukkit.craftbukkit.block.impl.CraftNetherWart::new);
//        register(net.minecraft.block.BlockNote.class, org.bukkit.craftbukkit.block.impl.CraftNote::new);
//        register(net.minecraft.block.BlockObserver.class, org.bukkit.craftbukkit.block.impl.CraftObserver::new);
//        register(net.minecraft.block.BlockPiston.class, org.bukkit.craftbukkit.block.impl.CraftPiston::new);
//        register(net.minecraft.block.BlockPistonExtension.class, org.bukkit.craftbukkit.block.impl.CraftPistonExtension::new);
//        register(net.minecraft.block.BlockPistonMoving.class, org.bukkit.craftbukkit.block.impl.CraftPistonMoving::new);
//        register(net.minecraft.block.BlockPortal.class, org.bukkit.craftbukkit.block.impl.CraftPortal::new);
//        register(net.minecraft.block.BlockPotatoes.class, org.bukkit.craftbukkit.block.impl.CraftPotatoes::new);
//        register(net.minecraft.block.BlockPoweredRail.class, org.bukkit.craftbukkit.block.impl.CraftPoweredRail::new);
//        register(net.minecraft.block.BlockPressurePlateBinary.class, org.bukkit.craftbukkit.block.impl.CraftPressurePlateBinary::new);
//        register(net.minecraft.block.BlockPressurePlateWeighted.class, org.bukkit.craftbukkit.block.impl.CraftPressurePlateWeighted::new);
//        register(net.minecraft.block.BlockPumpkinCarved.class, org.bukkit.craftbukkit.block.impl.CraftPumpkinCarved::new);
//        register(net.minecraft.block.BlockRedstoneComparator.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneComparator::new);
//        register(net.minecraft.block.BlockRedstoneLamp.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneLamp::new);
//        register(net.minecraft.block.BlockRedstoneOre.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneOre::new);
//        register(net.minecraft.block.BlockRedstoneTorch.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneTorch::new);
//        register(net.minecraft.block.BlockRedstoneTorchWall.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneTorchWall::new);
//        register(net.minecraft.block.BlockRedstoneWire.class, org.bukkit.craftbukkit.block.impl.CraftRedstoneWire::new);
//        register(net.minecraft.block.BlockReed.class, org.bukkit.craftbukkit.block.impl.CraftReed::new);
//        register(net.minecraft.block.BlockRepeater.class, org.bukkit.craftbukkit.block.impl.CraftRepeater::new);
//        register(net.minecraft.block.BlockRotatable.class, org.bukkit.craftbukkit.block.impl.CraftRotatable::new);
//        register(net.minecraft.block.BlockSapling.class, org.bukkit.craftbukkit.block.impl.CraftSapling::new);
//        register(net.minecraft.block.BlockScaffolding.class, org.bukkit.craftbukkit.block.impl.CraftScaffolding::new);
//        register(net.minecraft.block.BlockSeaPickle.class, org.bukkit.craftbukkit.block.impl.CraftSeaPickle::new);
//        register(net.minecraft.block.BlockShulkerBox.class, org.bukkit.craftbukkit.block.impl.CraftShulkerBox::new);
//        register(net.minecraft.block.BlockSkull.class, org.bukkit.craftbukkit.block.impl.CraftSkull::new);
//        register(net.minecraft.block.BlockSkullPlayer.class, org.bukkit.craftbukkit.block.impl.CraftSkullPlayer::new);
//        register(net.minecraft.block.BlockSkullPlayerWall.class, org.bukkit.craftbukkit.block.impl.CraftSkullPlayerWall::new);
//        register(net.minecraft.block.BlockSkullWall.class, org.bukkit.craftbukkit.block.impl.CraftSkullWall::new);
//        register(net.minecraft.block.BlockSmoker.class, org.bukkit.craftbukkit.block.impl.CraftSmoker::new);
//        register(net.minecraft.block.BlockSnow.class, org.bukkit.craftbukkit.block.impl.CraftSnow::new);
//        register(net.minecraft.block.BlockSoil.class, org.bukkit.craftbukkit.block.impl.CraftSoil::new);
//        register(net.minecraft.block.BlockStainedGlassPane.class, org.bukkit.craftbukkit.block.impl.CraftStainedGlassPane::new);
//        register(net.minecraft.block.BlockStairs.class, org.bukkit.craftbukkit.block.impl.CraftStairs::new);
//        register(net.minecraft.block.BlockStem.class, org.bukkit.craftbukkit.block.impl.CraftStem::new);
//        register(net.minecraft.block.BlockStemAttached.class, org.bukkit.craftbukkit.block.impl.CraftStemAttached::new);
//        register(net.minecraft.block.BlockStepAbstract.class, org.bukkit.craftbukkit.block.impl.CraftStepAbstract::new);
//        register(net.minecraft.block.BlockStoneButton.class, org.bukkit.craftbukkit.block.impl.CraftStoneButton::new);
//        register(net.minecraft.block.BlockStonecutter.class, org.bukkit.craftbukkit.block.impl.CraftStonecutter::new);
//        register(net.minecraft.block.BlockStructure.class, org.bukkit.craftbukkit.block.impl.CraftStructure::new);
//        register(net.minecraft.block.BlockSweetBerryBush.class, org.bukkit.craftbukkit.block.impl.CraftSweetBerryBush::new);
//        register(net.minecraft.block.BlockTNT.class, org.bukkit.craftbukkit.block.impl.CraftTNT::new);
//        register(net.minecraft.block.BlockTallPlant.class, org.bukkit.craftbukkit.block.impl.CraftTallPlant::new);
//        register(net.minecraft.block.BlockTallPlantFlower.class, org.bukkit.craftbukkit.block.impl.CraftTallPlantFlower::new);
//        register(net.minecraft.block.BlockTallSeaGrass.class, org.bukkit.craftbukkit.block.impl.CraftTallSeaGrass::new);
//        register(net.minecraft.block.BlockTorchWall.class, org.bukkit.craftbukkit.block.impl.CraftTorchWall::new);
//        register(net.minecraft.block.BlockTrapdoor.class, org.bukkit.craftbukkit.block.impl.CraftTrapdoor::new);
//        register(net.minecraft.block.BlockTripwire.class, org.bukkit.craftbukkit.block.impl.CraftTripwire::new);
//        register(net.minecraft.block.BlockTripwireHook.class, org.bukkit.craftbukkit.block.impl.CraftTripwireHook::new);
//        register(net.minecraft.block.BlockTurtleEgg.class, org.bukkit.craftbukkit.block.impl.CraftTurtleEgg::new);
//        register(net.minecraft.block.BlockVine.class, org.bukkit.craftbukkit.block.impl.CraftVine::new);
//        register(net.minecraft.block.BlockWallSign.class, org.bukkit.craftbukkit.block.impl.CraftWallSign::new);
//        register(net.minecraft.block.BlockWitherSkull.class, org.bukkit.craftbukkit.block.impl.CraftWitherSkull::new);
//        register(net.minecraft.block.BlockWitherSkullWall.class, org.bukkit.craftbukkit.block.impl.CraftWitherSkullWall::new);
//        register(net.minecraft.block.BlockWoodButton.class, org.bukkit.craftbukkit.block.impl.CraftWoodButton::new);
//        //</editor-fold>
//    }

    private static void register(Class<? extends Block> nms, Function<BlockPattern, MagmaBlockData> bukkit) {
        Preconditions.checkState(MAP.put(nms, bukkit) == null, "Duplicate mapping %s->%s", nms, bukkit);
    }

    public static MagmaBlockData newData(Material material, String data) {
        Preconditions.checkArgument(material == null || material.isBlock(), "Cannot get data for not block %s", material);

        BlockPattern blockData;
        Block block = CraftMagicNumbers.getBlock(material);
        Map<BlockStateMatcher<?>, Comparable<?>> parsed = null;

        // Data provided, use it
        if (data != null) {
            try {
                // Material provided, force that material in
                if (block != null) {
                    data = IRegistry.BLOCK.getKey(block) + data;
                }

                StringReader reader = new StringReader(data);
                ArgumentBlock arg = new ArgumentBlock(reader, false).a(false);
                Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data: " + data);

                blockData = arg.getBlockData();
                parsed = arg.getStateMap();
            } catch (CommandSyntaxException ex) {
                throw new IllegalArgumentException("Could not parse data: " + data, ex);
            }
        } else {
            blockData = block.getBlockData();
        }

        MagmaBlockData craft = fromData(blockData);
        craft.parsedStates = parsed;
        return craft;
    }

    public static MagmaBlockData fromData(BlockPattern data) {
        return MAP.getOrDefault(data.getBlock().getClass(), MagmaBlockData::new).apply(data);
    }
}
