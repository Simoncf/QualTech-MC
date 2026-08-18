package com.qualtech;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyCellBlockEntity extends BlockEntity {
    public static final int CAPACITY = 100_000;
    public static final int MAX_TRANSFER = 1_000;
    // How much a right-click debug charge adds/removes, for testing without a cable-based RF source
    private static final int DEBUG_CHARGE_AMOUNT = 10_000;

    private final QualTechEnergyStorage energyStorage = new QualTechEnergyStorage(CAPACITY, MAX_TRANSFER, this::setChanged);

    public EnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(QualTech.ENERGY_CELL_BE.get(), pos, state);
    }

    public QualTechEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public void debugCharge() {
        energyStorage.receiveEnergy(DEBUG_CHARGE_AMOUNT, false);
    }

    public void debugDrain() {
        energyStorage.extractEnergy(DEBUG_CHARGE_AMOUNT, false);
    }

    public void reportEnergyTo(Player player) {
        player.displayClientMessage(Component.literal(
                energyStorage.getEnergyStored() + " / " + energyStorage.getMaxEnergyStored() + " RF"), true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Energy", energyStorage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(registries, tag.get("Energy"));
        }
    }
}
