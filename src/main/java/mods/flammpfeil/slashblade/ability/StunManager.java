package mods.flammpfeil.slashblade.ability;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mods.flammpfeil.slashblade.entity.ai.EntityAIStun;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Created by Furia on 15/06/20.
 */
public class StunManager {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event){
        if(!(event.getEntity() instanceof EntityLiving)) return;
        EntityLiving entity = (EntityLiving) event.getEntity();

        entity.tasks.addTask(-1,new EntityAIStun(entity));
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event){
        EntityLivingBase target = event.getEntityLiving();
        if(target == null) return;
        if(target.world == null) return;

        long timeout = target.getEntityData().getLong(EntityAIStun.StunTimeout);
        if(timeout == 0) return;
        timeout = timeout - target.world.getGameTime();
        if(timeout <= 0 || EntityAIStun.timeoutLimit < timeout){
            target.getEntityData().removeTag(EntityAIStun.StunTimeout);
            return;
        }

        if(target.motionY < 0)
            target.motionY *= 0.5f;
    }

    public static void setStun(EntityLivingBase target, long duration){
        if(target.world == null) return;
        if(!(target instanceof EntityLiving)) return;
        if(duration <= 0) return;

        duration = Math.min(duration, EntityAIStun.timeoutLimit);
        target.getEntityData().setLong(EntityAIStun.StunTimeout,target.world.getGameTime() + duration);
    }

    public static void removeStun(EntityLivingBase target){
        if(target.world == null) return;
        if(!(target instanceof EntityLiving)) return;
        target.getEntityData().removeTag(EntityAIStun.StunTimeout);
        target.getEntityData().removeTag(FreezeTimeout);
    }

    static final String FreezeTimeout = "FreezeTimeout";
    static final long freezeLimit = 200;

    @SubscribeEvent
    public void onEntityCanUpdate(EntityEvent.CanUpdate event){
        if(event.isCanceled()) return;
        Entity target = event.getEntity();
        if(target == null) return;
        if(target.world == null) return;

        long timeout = target.getEntityData().getLong(FreezeTimeout);
        if(timeout == 0) return;

        long timeLeft = timeout - target.world.getGameTime();
        if(timeLeft <= 0 || freezeLimit < timeLeft){
            target.getEntityData().removeTag(FreezeTimeout);
            return;
        }else{
            event.setCanUpdate(false);
        }
    }

    public static void setFreeze(EntityLivingBase target, long duration){
        if(target.world == null) return;
        if(!(target instanceof EntityLiving)) return;
        if(duration <= 0) return;

        duration = Math.min(duration, freezeLimit);
        long oldTimeout = target.getEntityData().getLong(FreezeTimeout);
        long timeout = target.world.getGameTime() + duration;

        timeout = Math.max(oldTimeout, timeout);

        target.getEntityData().setLong(FreezeTimeout,timeout);
    }
}
