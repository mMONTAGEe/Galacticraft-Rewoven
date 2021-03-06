/*
 * Copyright (c) 2020 HRZN LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hrznstudio.galacticraft.items;

import com.hrznstudio.galacticraft.component.GalacticraftComponents;
import com.hrznstudio.galacticraft.fluids.GalacticraftFluids;
import com.hrznstudio.galacticraft.tag.GalacticraftTags;
import com.hrznstudio.galacticraft.util.OxygenUtils;
import io.github.cottonmc.component.UniversalComponents;
import io.github.cottonmc.component.api.ActionType;
import io.github.cottonmc.component.fluid.impl.ItemTankComponent;
import io.github.cottonmc.component.item.InventoryComponent;
import io.github.fablabsmc.fablabs.api.fluidvolume.v1.FluidVolume;
import io.github.fablabsmc.fablabs.api.fluidvolume.v1.Fraction;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class OxygenTankItem extends Item implements ItemComponentCallback{
    public OxygenTankItem(Settings settings) {
        super(settings);
        ItemComponentCallback.registerSelf(this);
    }

    @Override
    public void appendStacks(ItemGroup itemGroup_1, DefaultedList<ItemStack> list) {
        if (this.isIn(itemGroup_1)) {
            ItemStack stack = new ItemStack(this);
            list.add(stack);
            stack = stack.copy();
            ComponentProvider.fromItemStack(stack).getComponent(UniversalComponents.TANK_COMPONENT).setFluid(0, new FluidVolume(GalacticraftFluids.OXYGEN, ComponentProvider.fromItemStack(stack).getComponent(UniversalComponents.TANK_COMPONENT).getMaxCapacity(0)));
            list.add(stack);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> lines, TooltipContext context) {
        lines.add(new TranslatableText("tooltip.galacticraft-rewoven.oxygen_remaining", ((int)(OxygenUtils.getOxygen(stack).doubleValue() * 100.0D) + "/" + (int)(OxygenUtils.getMaxOxygen(stack).doubleValue() * 100.0D))));
        super.appendTooltip(stack, world, lines, context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) { //should sync with server
        InventoryComponent component = GalacticraftComponents.GEAR_INVENTORY_COMPONENT.get(player);
        if (component.getStack(6).isEmpty()) {
            component.setStack(6, player.getStackInHand(hand).copy());
            return new TypedActionResult<>(ActionResult.SUCCESS, ItemStack.EMPTY);
        } else if (component.getStack(7).isEmpty()) {
            component.setStack(7, player.getStackInHand(hand).copy());
            return new TypedActionResult<>(ActionResult.SUCCESS, ItemStack.EMPTY);
        }
        return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand));
    }

    @Override
    public void initComponents(ItemStack itemStack, ComponentContainer<CopyableComponent<?>> componentContainer) {
        ItemTankComponent component = new ItemTankComponent(1, Fraction.of(1, 100).multiply(Fraction.ofWhole(getMaxDamage()))) {
            @Override
            public FluidVolume insertFluid(int tank, FluidVolume fluid, ActionType action) {
                if (fluid.getFluid().isIn(GalacticraftTags.OXYGEN) || fluid.isEmpty()) {
                    return super.insertFluid(tank, fluid, action);
                } else {
                    return fluid;
                }
            }

            @Override
            public FluidVolume insertFluid(FluidVolume fluid, ActionType action) {
                return insertFluid(0, fluid, action);
            }

            @Override
            public void setFluid(int slot, FluidVolume stack) {
                if (stack.getFluid().isIn(GalacticraftTags.OXYGEN) || stack.isEmpty()) {
                    super.setFluid(slot, stack);
                }
            }
        };
        component.listen(() -> itemStack.setDamage(getMaxDamage() - (int)(component.getContents(0).getAmount().doubleValue() * 100.0D)));
        componentContainer.put(UniversalComponents.TANK_COMPONENT, component);
    }
}
