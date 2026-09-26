package online.remind.remind.entity.spirits;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BaseDreamEaterEntity extends TamableAnimal {

    int hp, str, mag, def, lvl;

    public BaseDreamEaterEntity(EntityType<? extends TamableAnimal> type, Level worldIn) {
        super(type, worldIn);

    }

    public int getStr() {
        return str;
    }

    public void setStr(int str) {
        this.str = str;
    }


    public int getMag() {
        return mag;
    }

    public void setMag(int mag) {
        this.mag = mag;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public CompoundTag getDreamEaterStats() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("hp", hp);
        tag.putInt("str", str);
        tag.putInt("mag", mag);
        tag.putInt("def", def);
        tag.putInt("lvl", lvl);
        return tag;
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        hp = tag.getInt("hp");
        str = tag.getInt("str");
        mag = tag.getInt("mag");
        def = tag.getInt("def");
        lvl = tag.getInt("lvl");
        super.readAdditionalSaveData(tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("hp", hp);
        tag.putInt("str", str);
        tag.putInt("mag", mag);
        tag.putInt("def", def);
        tag.putInt("lvl", lvl);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("hp", hp);
        tag.putInt("str", str);
        tag.putInt("mag", mag);
        tag.putInt("def", def);
        tag.putInt("lvl", lvl);
        return tag;
    }


    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}

