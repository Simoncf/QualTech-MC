package com.qualtech;

import net.neoforged.neoforge.energy.EnergyStorage;

// A plain EnergyStorage doesn't expose a way to set its stored amount (e.g. for NBT loading)
// or notify anyone when it changes, so this subclass adds both on top of NeoForge's RF/FE storage.
public class QualTechEnergyStorage extends EnergyStorage {
    private final Runnable onChange;

    public QualTechEnergyStorage(int capacity, int maxTransfer, Runnable onChange) {
        super(capacity, maxTransfer, maxTransfer);
        this.onChange = onChange;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = super.receiveEnergy(toReceive, simulate);
        if (!simulate && received > 0) {
            onChange.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int extracted = super.extractEnergy(toExtract, simulate);
        if (!simulate && extracted > 0) {
            onChange.run();
        }
        return extracted;
    }
}
