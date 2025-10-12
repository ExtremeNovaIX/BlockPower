package BlockPower.Skills.ComboSkills.Client;

import BlockPower.Skills.ComboSkills.ComboSkillType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端缓存连击数据，用于渲染连携技图标
 */
@OnlyIn(Dist.CLIENT)
public class ClientComboData {
    //用于记录当前可释放的连携技
    private static final List<ComboSkillType> activeComboSkills = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ClientComboData.class);

    /**
     * 添加可释放的连携技
     */
    public static void addActiveChainSkill(ComboSkillType comboSkillType) {
        log.info("ClientComboData add: {}", comboSkillType);
        if (activeComboSkills.contains(comboSkillType)) {
            return;
        }
        activeComboSkills.add(comboSkillType);
        log.info("ClientComboData: activeComboSkills: {}", activeComboSkills);
    }

     /**
     * 获取当前可释放的连携技
     */
    public static List<ComboSkillType> getActiveComboSkills() {
        return activeComboSkills;
    }

    public static ComboSkillType getFirstActiveComboSkill() {
        return activeComboSkills.isEmpty() ? null : activeComboSkills.get(0);
    }

    /**
     * 移除已释放的连携技
     */
    public static void removeActiveChainSkill(ComboSkillType comboSkillType) {
        log.info("ClientComboData remove: {}", comboSkillType);
        activeComboSkills.remove(comboSkillType);
        log.info("ClientComboData: activeComboSkills: {}", activeComboSkills);
    }
}
