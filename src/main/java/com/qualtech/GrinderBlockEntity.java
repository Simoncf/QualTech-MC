package com.qualtech;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class GrinderBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    public static final int CAPACITY = 10_000;
    public static final int MAX_RECEIVE = 200;
    private static final int ENERGY_PER_TICK = 40;
    private static final int MAX_PROGRESS = 100;

    // Ore -> doubled raw material, and raw material -> doubled dust: a two-stage processing chain,
    // with dust smelting back into ingots (see data/qualtech/recipe/*_dust_smelting.json)
    private static final Map<Item, ItemStack> RECIPES = Map.of(
            Items.IRON_ORE, new ItemStack(Items.RAW_IRON, 2),
            Items.DEEPSLATE_IRON_ORE, new ItemStack(Items.RAW_IRON, 2),
            Items.GOLD_ORE, new ItemStack(Items.RAW_GOLD, 2),
            Items.DEEPSLATE_GOLD_ORE, new ItemStack(Items.RAW_GOLD, 2),
            Items.COPPER_ORE, new ItemStack(Items.RAW_COPPER, 2),
            Items.DEEPSLATE_COPPER_ORE, new ItemStack(Items.RAW_COPPER, 2),
            Items.RAW_IRON, new ItemStack(QualTech.IRON_DUST.get(), 2),
            Items.RAW_GOLD, new ItemStack(QualTech.GOLD_DUST.get(), 2),
            Items.RAW_COPPER, new ItemStack(QualTech.COPPER_DUST.get(), 2));

    private final QualTechEnergyStorage energyStorage = new QualTechEnergyStorage(CAPACITY, MAX_RECEIVE, this::setChanged);
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // The output slot is only ever filled internally by processItem(); automation can only extract from it
            return slot == SLOT_INPUT && RECIPES.containsKey(stack.getItem());
        }
    };

    private int progress = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> progress;
                case 3 -> MAX_PROGRESS;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergyStored(value);
                case 2 -> progress = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(QualTech.ORE_GRINDER_BE.get(), pos, state);
    }

    public QualTechEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    public boolean canAcceptInput(ItemStack stack) {
        return itemHandler.isItemValid(SLOT_INPUT, stack);
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, GrinderBlockEntity blockEntity) {
        if (blockEntity.canProcess()) {
            blockEntity.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
            blockEntity.progress++;
            if (blockEntity.progress >= MAX_PROGRESS) {
                blockEntity.processItem();
                blockEntity.progress = 0;
            }
            blockEntity.setChanged();
        } else if (blockEntity.progress > 0) {
            blockEntity.progress = 0;
            blockEntity.setChanged();
        }
    }

    private boolean canProcess() {
        ItemStack input = itemHandler.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty() || energyStorage.getEnergyStored() < ENERGY_PER_TICK) {
            return false;
        }
        ItemStack result = RECIPES.get(input.getItem());
        if (result == null) {
            return false;
        }
        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        return output.isEmpty()
                || (ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize());
    }

    private void processItem() {
        ItemStack input = itemHandler.getStackInSlot(SLOT_INPUT);
        ItemStack result = RECIPES.get(input.getItem()).copy();
        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        input.shrink(1);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.qualtech.ore_grinder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GrinderMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.put("Energy", energyStorage.serializeNBT(registries));
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(registries, tag.get("Energy"));
        }
        progress = tag.getInt("Progress");
    }
}
