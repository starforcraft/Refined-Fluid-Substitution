package com.ultramega.refinedfluidsubstitution.common.mixin;

import com.ultramega.refinedfluidsubstitution.common.util.MainNetworkNodeAccessor;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractNetworkNodeContainerBlockEntity.class, remap = false)
public abstract class AbstractNetworkNodeContainerBlockEntityMixin implements MainNetworkNodeAccessor {
    @Shadow(remap = false)
    @Final
    protected NetworkNode mainNetworkNode;

    @Override
    public NetworkNode rfs$getMainNetworkNode() {
        return this.mainNetworkNode;
    }
}
