package com.simibubi.create.lib.transfer.fluid;

import java.util.function.Predicate;

import net.minecraft.nbt.CompoundTag;

public class FluidTank implements IFluidHandler {
	protected FluidStack fluid = FluidStack.EMPTY;
	protected long capacity;
	protected Predicate<FluidStack> validator;

	public FluidTank(FluidStack fluid, long capacity) {
		this(capacity);
		this.fluid = fluid;
	}

	public FluidTank(long capacity) {
		this(capacity, e -> true);
	}

	public FluidTank(long capacity, Predicate<FluidStack> validator) {
		this.capacity = capacity;
		this.validator = validator;
	}

	public FluidTank setValidator(Predicate<FluidStack> validator) {
		if (validator != null) {
			this.validator = validator;
		}
		return this;
	}

	public FluidTank setCapacity(long capacity) {
		this.capacity = capacity;
		return this;
	}

	public long getCapacity() {
		return capacity;
	}

	public FluidStack getFluid() {
		return fluid;
	}

	public void setFluid(FluidStack fluid) {
		this.fluid = fluid;
	}

	public CompoundTag writeToNBT(CompoundTag tag) {
		fluid.writeToNBT(tag);
		tag.putLong("Capacity", capacity);
		return tag;
	}

	public FluidTank readFromNBT(CompoundTag tag) {
		this.fluid = FluidStack.loadFluidStackFromNBT(tag);
		if (tag.contains("Capacity")) this.capacity = tag.getLong("Capacity");
		return this;
	}

	public boolean isEmpty() {
		return getFluid() == null || getFluid().isEmpty();
	}

	public long getFluidAmount() {
		return getFluid().getAmount();
	}

	public long getSpace() {
		return Math.max(0, capacity - getFluid().getAmount());
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return fluid;
	}

	@Override
	public long getTankCapacity(int tank) {
		return getCapacity();
	}

	@Override
	public long fill(FluidStack resource, boolean sim) {
		if (resource.isEmpty() || !isFluidValid(0, resource)) {
			return 0;
		}
		if (sim) {
			if (fluid.isEmpty()) {
				return Math.min(capacity, resource.getAmount());
			}
			if (!fluid.isFluidEqual(resource)) {
				return 0;
			}
			return Math.min(capacity - fluid.getAmount(), resource.getAmount());
		}
		if (fluid.isEmpty()) {
			fluid = resource.copy().setAmount(Math.min(capacity, resource.getAmount()));
			onContentsChanged();
			return fluid.getAmount();
		}
		if (!fluid.isFluidEqual(resource)) {
			return 0;
		}
		long filled = capacity - fluid.getAmount();

		if (resource.getAmount() < filled) {
			fluid.grow(resource.getAmount());
			filled = resource.getAmount();
		}
		else {
			fluid.setAmount(capacity);
		}
		if (filled > 0)
			onContentsChanged();
		return filled;
	}

	@Override
	public FluidStack drain(FluidStack stack, boolean sim) {
		return drain(stack.getAmount(), sim);
	}

	@Override
	public FluidStack drain(long amount, boolean sim) {
		long canRemove = fluid.getAmount();
		if (amount > canRemove) amount = canRemove;
		FluidStack out = fluid.copy().setAmount(amount);
		if (!sim) {
			fluid.shrink(amount);
			if (fluid.isEmpty()) fluid = FluidStack.EMPTY;
			onContentsChanged();
		}

		return out;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return validator.test(stack);
	}

	protected void onContentsChanged() {
	}
}
