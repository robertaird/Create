package com.simibubi.create.lib.mixin.common;

import java.util.Collection;

import com.simibubi.create.lib.event.EntityReadExtraDataCallback;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.Create;
import com.simibubi.create.lib.block.CustomRunningEffectsBlock;
import com.simibubi.create.lib.event.EntityEyeHeightCallback;
import com.simibubi.create.lib.event.StartRidingCallback;
import com.simibubi.create.lib.extensions.EntityExtensions;
import com.simibubi.create.lib.util.EntityHelper;
import com.simibubi.create.lib.util.ListenerProvider;
import com.simibubi.create.lib.util.MixinHelper;
import com.simibubi.create.lib.util.NBTSerializable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtensions, NBTSerializable {
	@Shadow
	public Level level;
	@Shadow
	private float eyeHeight;
	@Unique
	private CompoundTag create$extraCustomData;
	@Unique
	private Collection<ItemEntity> create$captureDrops = null;

	@Shadow
	protected abstract void readAdditionalSaveData(CompoundTag compoundTag);

	@Inject(at = @At("TAIL"), method = "<init>")
	public void create$entityInit(EntityType<?> entityType, Level world, CallbackInfo ci) {
		int newEyeHeight = EntityEyeHeightCallback.EVENT.invoker().onEntitySize((Entity) (Object) this);
		if (newEyeHeight != -1)
			eyeHeight = newEyeHeight;
	}

	// CAPTURE DROPS

	@Inject(
			method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
			locals = LocalCapture.CAPTURE_FAILHARD,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/item/ItemEntity;setDefaultPickUpDelay()V",
					shift = At.Shift.AFTER
			),
			cancellable = true
	)
	public void create$spawnAtLocation(ItemStack stack, float f, CallbackInfoReturnable<ItemEntity> cir, ItemEntity itemEntity) {
		if (create$captureDrops != null) {
			create$captureDrops.add(itemEntity);
			cir.setReturnValue(itemEntity);
		}
	}

	@Unique
	@Override
	public Collection<ItemEntity> create$captureDrops() {
		return create$captureDrops;
	}

	@Unique
	@Override
	public Collection<ItemEntity> create$captureDrops(Collection<ItemEntity> value) {
		Collection<ItemEntity> ret = create$captureDrops;
		create$captureDrops = value;
		return ret;
	}

	// EXTRA CUSTOM DATA

	@Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	public void create$beforeWriteCustomData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (create$extraCustomData != null && !create$extraCustomData.isEmpty()) {
			Create.LOGGER.debug("writing custom data to entity [{}]", this);
			tag.put(EntityHelper.EXTRA_DATA_KEY, create$extraCustomData);
		}
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	public void create$beforeReadCustomData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains(EntityHelper.EXTRA_DATA_KEY)) {
			create$extraCustomData = tag.getCompound(EntityHelper.EXTRA_DATA_KEY);
		}
		EntityReadExtraDataCallback.EVENT.invoker().onLoad((Entity) (Object) this, create$extraCustomData);
	}

	// RUNNING EFFECTS

	@Inject(
			method = "spawnSprintParticle",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	public void create$spawnSprintParticle(CallbackInfo ci, int i, int j, int k, BlockPos blockPos) {
		BlockState state = level.getBlockState(blockPos);
		if (state.getBlock() instanceof CustomRunningEffectsBlock custom) {
			if (custom.addRunningEffects(state, level, blockPos, (Entity) (Object) this)) ci.cancel();
		}
	}

	@Inject(method = "discard", at = @At("HEAD"))
	public void create$discard(CallbackInfo ci) {
		if (this instanceof ListenerProvider) {
			((ListenerProvider) this).invalidate();
		}
	}

	@Inject(
			method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z",
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	public void create$startRiding(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
		if (StartRidingCallback.EVENT.invoker().onStartRiding(MixinHelper.cast(this), entity) == InteractionResult.FAIL) {
			cir.setReturnValue(false);
		}
	}

	@Unique
	@Override
	public CompoundTag create$getExtraCustomData() {
		if (create$extraCustomData == null) {
			create$extraCustomData = new CompoundTag();
		}
		return create$extraCustomData;
	}

	@Unique
	@Override
	public CompoundTag create$serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		String id = EntityHelper.getEntityString(MixinHelper.cast(this));

		if (id != null) {
			nbt.putString("id", id);
		}

		return nbt;
	}

	@Unique
	@Override
	public void create$deserializeNBT(CompoundTag nbt) {
		readAdditionalSaveData(nbt);
	}
}
