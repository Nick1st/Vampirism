package de.teamlapen.vampirism.modcompat.guide;

import com.google.common.collect.Maps;
import de.maxanier.guideapi.api.IGuideBook;
import de.maxanier.guideapi.api.IPage;
import de.maxanier.guideapi.api.impl.Book;
import de.maxanier.guideapi.api.impl.BookBinder;
import de.maxanier.guideapi.api.impl.abstraction.CategoryAbstract;
import de.maxanier.guideapi.api.impl.abstraction.EntryAbstract;
import de.maxanier.guideapi.api.util.PageHelper;
import de.maxanier.guideapi.category.CategoryItemStack;
import de.maxanier.guideapi.page.PageEntity;
import de.maxanier.guideapi.page.PageImage;
import de.maxanier.guideapi.page.PageText;
import de.maxanier.guideapi.page.PageTextImage;
import de.teamlapen.lib.VampLib;
import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.items.ExtendedPotionMix;
import de.teamlapen.vampirism.blocks.AltarPillarBlock;
import de.teamlapen.vampirism.client.core.ModKeys;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.*;
import de.teamlapen.vampirism.entity.hunter.BasicHunterEntity;
import de.teamlapen.vampirism.items.BloodBottleItem;
import de.teamlapen.vampirism.modcompat.guide.pages.PageHolderWithLinks;
import de.teamlapen.vampirism.modcompat.guide.pages.PagePotionTableMix;
import de.teamlapen.vampirism.modcompat.guide.pages.PageTable;
import de.teamlapen.vampirism.player.hunter.HunterLevelingConf;
import de.teamlapen.vampirism.player.hunter.actions.HunterActions;
import de.teamlapen.vampirism.player.hunter.skills.HunterSkills;
import de.teamlapen.vampirism.player.vampire.VampireLevelingConf;
import de.teamlapen.vampirism.util.REFERENCE;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.awt.Color;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;


@de.maxanier.guideapi.api.GuideBook
public class GuideBook implements IGuideBook {

    private static final Logger LOGGER = LogManager.getLogger();
    private final static String IMAGE_BASE = "vampirismguide:textures/images/";
    private static Book guideBook;
    private static final Map<ResourceLocation, EntryAbstract> links = Maps.newHashMap();


    static void buildCategories(List<CategoryAbstract> categories) {
        LOGGER.debug("Building content");
        long start = System.currentTimeMillis();
        categories.add(new CategoryItemStack(buildOverview(), translateComponent("guide.vampirism.overview.title"), new ItemStack(ModItems.vampire_fang)));
        categories.add(new CategoryItemStack(buildVampire(), translateComponent("guide.vampirism.vampire.title"), BloodBottleItem.getStackWithDamage(BloodBottleItem.AMOUNT)));
        categories.add(new CategoryItemStack(buildHunter(), translateComponent("guide.vampirism.hunter.title"), new ItemStack(ModItems.human_heart)));
        categories.add(new CategoryItemStack(buildCreatures(), translateComponent("guide.vampirism.entity.title"), new ItemStack(Items.ZOMBIE_HEAD)));
        categories.add(new CategoryItemStack(buildWorld(), translateComponent("guide.vampirism.world.title"), new ItemStack(ModBlocks.cursed_earth)));
        categories.add(new CategoryItemStack(buildItems(), translateComponent("guide.vampirism.items.title"), new ItemStack(Items.APPLE)));
        categories.add(new CategoryItemStack(buildBlocks(), translateComponent("guide.vampirism.blocks.title"), new ItemStack(ModBlocks.castle_block_dark_brick)));
        MinecraftForge.EVENT_BUS.post(new VampirismGuideBookCategoriesEvent(categories));
        LOGGER.debug("Built content in {} ms", System.currentTimeMillis() - start);
    }

    /**
     * @return The entry registered with the given location. Can be null
     */
    public static
    @Nullable
    EntryAbstract getLinkedEntry(ResourceLocation location) {
        return links.get(location);
    }

    private static Map<ResourceLocation, EntryAbstract> buildOverview() {
        Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
        String base = "guide.vampirism.overview.";

        List<IPage> introPages = new ArrayList<>();
        introPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "intro.text")));
        entries.put(new ResourceLocation(base + "intro"), new EntryText(introPages, translateComponent(base + "intro")));

        List<IPage> gettingStartedPages = new ArrayList<>();
        IPage p = new PageText(translateComponent(base + "getting_started.text"));
        p = new PageHolderWithLinks(p).addLink("guide.vampirism.vampire.getting_started").addLink("guide.vampirism.hunter.getting_started");
        gettingStartedPages.add(p);
        entries.put(new ResourceLocation(base + "getting_started"), new EntryText(gettingStartedPages, translateComponent(base + "getting_started")));

        List<IPage> configPages = new ArrayList<>();
        configPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "config.text")));
        configPages.addAll(PageHelper.pagesForLongText(ITextProperties.func_240655_a_(translateComponent(base + "config.general.text"), translateComponent(base + "config.general.examples"))));
        configPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "config.balance.text")));
        entries.put(new ResourceLocation(base + "config"), new EntryText(configPages, translateComponent(base + "config")));

        List<IPage> troublePages = new ArrayList<>();
        troublePages.addAll(PageHelper.pagesForLongText(translateComponent(base + "trouble.text")));
        GuideHelper.addLinks(troublePages, new PageHolderWithLinks.URLLink(translateComponent(base + "trouble"), URI.create("https://github.com/TeamLapen/Vampirism/wiki/Troubleshooting")));
        entries.put(new ResourceLocation(base + "trouble"), new EntryText(troublePages, translateComponent(base + "trouble")));

        List<IPage> devPages = new ArrayList<>();
        PageHolderWithLinks.URLLink helpLink = new PageHolderWithLinks.URLLink(new StringTextComponent("How to help"), URI.create("https://github.com/TeamLapen/Vampirism/wiki#how-you-can-help"));
        devPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "dev.text")), helpLink));
        entries.put(new ResourceLocation(base + "dev"), new EntryText(devPages, translateComponent(base + "dev")));

        List<IPage> supportPages = new ArrayList<>();
        supportPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "support.text")));
        PageHolderWithLinks.URLLink linkCurseForge = new PageHolderWithLinks.URLLink(new StringTextComponent("CurseForge"), URI.create(REFERENCE.CURSEFORGE_LINK));

        GuideHelper.addLinks(supportPages, linkCurseForge, new ResourceLocation(base + "dev"));
        entries.put(new ResourceLocation(base + "support"), new EntryText(supportPages, translateComponent(base + "support")));

        List<IPage> creditsPages = new ArrayList<>();
        String lang = VampLib.proxy.getActiveLanguage();
        String credits = "§lDeveloper:§r\nMaxanier\nCheaterpaul\n§lThanks to:§r\nMistadon\nwildbill22\n1LiterZinalco\nAlis\ndimensionpainter\nS_olace\nPiklach\n\n§lTranslators:§r\n§b" + lang + "§r\n" + translateComponent("text.vampirism.translators").getString();
        creditsPages.addAll(PageHelper.pagesForLongText(translateComponent(credits)));
        entries.put(new ResourceLocation(base + "credits"), new EntryText(creditsPages, translateComponent(base + "credits")));
        links.putAll(entries);
        return entries;
    }

    private static String loc(Block b) {
        return UtilLib.translate(b.getTranslationKey());
    }

    private static String loc(Item i) {
        return UtilLib.translate(i.getTranslationKey());
    }

    private static Map<ResourceLocation, EntryAbstract> buildVampire() {
        Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
        String base = "guide.vampirism.vampire.";

        List<IPage> gettingStarted = new ArrayList<>();
        gettingStarted.addAll(PageHelper.pagesForLongText(translateComponent(base + "getting_started.become")));
        gettingStarted.addAll(PageHelper.pagesForLongText(translateComponent(base + "getting_started.as_vampire")));
        gettingStarted.addAll(PageHelper.pagesForLongText(translateComponent(base + "getting_started.zombie")));
        gettingStarted.addAll(PageHelper.pagesForLongText(translateComponent(base + "getting_started.blood", new TranslationTextComponent(ModKeys.getKeyBinding(ModKeys.KEY.SUCK).getTranslationKey()))));
        gettingStarted.addAll(PageHelper.pagesForLongText(ITextProperties.func_240655_a_(translateComponent(base + "getting_started.level"), translateComponent(base + "getting_started.level2"))));

        entries.put(new ResourceLocation(base + "getting_started"), new EntryText(gettingStarted, translateComponent(base + "getting_started")));

        List<IPage> bloodPages = new ArrayList<>();
        bloodPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "blood.text", loc(ModItems.blood_bottle), loc(Items.GLASS_BOTTLE))));
        bloodPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "blood.storage", loc(ModBlocks.blood_container))), new ResourceLocation("guide.vampirism.blocks.blood_container")));
        bloodPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "blood.biteable_creatures")), new PageHolderWithLinks.URLLink(new StringTextComponent("Biteable Creatures"), URI.create("https://github.com/TeamLapen/Vampirism/wiki/Biteable-Creatures"))));
        entries.put(new ResourceLocation(base + "blood"), new EntryText(bloodPages, translateComponent(base + "blood")));

        VampireLevelingConf levelingConf = VampireLevelingConf.getInstance();
        List<IPage> levelingPages = new ArrayList<>();
        levelingPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "leveling.intro")));
        String altarOfInspiration = "§l" + loc(ModBlocks.altar_inspiration) + "§r\n§o" + translate(base + "leveling.inspiration.reach") + "§r\n";
        altarOfInspiration += translate(base + "leveling.inspiration.text") + "\n";
        altarOfInspiration += translate(base + "leveling.inspiration.requirements", levelingConf.getRequiredBloodForAltarInspiration(2), levelingConf.getRequiredBloodForAltarInspiration(3), levelingConf.getRequiredBloodForAltarInspiration(4));
        levelingPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new StringTextComponent(altarOfInspiration)), new ResourceLocation("guide.vampirism.blocks.altar_inspiration")));

        String altarOfInfusion = "§l" + loc(ModBlocks.altar_infusion) + "§r\n§o" + translate(base + "leveling.infusion.reach") + "§r\n";
        altarOfInfusion += translate(base + "leveling.infusion.intro", loc(ModBlocks.altar_infusion), loc(ModBlocks.altar_pillar), loc(ModBlocks.altar_tip));
        levelingPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new StringTextComponent(altarOfInfusion)), new ResourceLocation("guide.vampirism.blocks.altar_infusion")));
        StringBuilder blocks = new StringBuilder();
        for (AltarPillarBlock.EnumPillarType t : AltarPillarBlock.EnumPillarType.values()) {
            if (t == AltarPillarBlock.EnumPillarType.NONE) continue;
            blocks.append(translate(t.fillerBlock.getTranslationKey())).append("(").append(t.getValue()).append("),");
        }
        levelingPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "leveling.infusion.structure", blocks.toString())));
        String items = loc(ModItems.human_heart) + ", " + loc(ModItems.pure_blood_0) + ", " + loc(ModItems.vampire_book);
        levelingPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "leveling.infusion.items", items)), new ResourceLocation("guide.vampirism.items.human_heart"), new ResourceLocation("guide.vampirism.items.pure_blood_0"), new ResourceLocation("guide.vampirism.items.vampire_book")));
        PageTable.Builder requirementsBuilder = new PageTable.Builder(5);
        requirementsBuilder.addUnlocLine("text.vampirism.level_short", base + "leveling.infusion.req.structure_points", ModItems.pure_blood_0.getTranslationKey(), base + "leveling.infusion.req.heart", base + "leveling.infusion.req.book");
        requirementsBuilder.addLine("5", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(5), "0", "5", "1");
        requirementsBuilder.addLine("6", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(6), "1 Purity(1)", "5", "1");
        requirementsBuilder.addLine("7", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(7), "1 Purity(1)", "10", "1");
        requirementsBuilder.addLine("8", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(8), "1 Purity(2)", "10", "1");
        requirementsBuilder.addLine("9", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(9), "1 Purity(2)", "10", "1");
        requirementsBuilder.addLine("10", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(10), "1 Purity(3)", "15", "1");
        requirementsBuilder.addLine("11", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(11), "1 Purity(3)", "15", "1");
        requirementsBuilder.addLine("12", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(12), "1 Purity(4)", "20", "1");
        requirementsBuilder.addLine("13", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(13), "2 Purity(4)", "20", "1");
        requirementsBuilder.addLine("14", VampireLevelingConf.getInstance().getRequiredStructureLevelAltarInfusion(14), "2 Purity(5)", "25", "1");
        requirementsBuilder.setHeadline(translateComponent(base + "leveling.infusion.req"));
        PageHolderWithLinks requirementTable = new PageHolderWithLinks(requirementsBuilder.build());
        requirementTable.addLink(new ResourceLocation("guide.vampirism.items.human_heart"));
        requirementTable.addLink(new ResourceLocation("guide.vampirism.items.vampire_book"));
        requirementTable.addLink(new ResourceLocation("guide.vampirism.items.pure_blood_0"));
        levelingPages.add(requirementTable);

        levelingPages.add(new PageTextImage(translateComponent(base + "leveling.infusion.image1"), new ResourceLocation(IMAGE_BASE + "infusion1.png"), false));
        levelingPages.add(new PageTextImage(translateComponent(base + "leveling.infusion.image2"), new ResourceLocation(IMAGE_BASE + "infusion2.png"), false));
        levelingPages.add(new PageTextImage(translateComponent(base + "leveling.infusion.image3"), new ResourceLocation(IMAGE_BASE + "infusion3.png"), false));
        levelingPages.add(new PageTextImage(translateComponent(base + "leveling.infusion.image4"), new ResourceLocation(IMAGE_BASE + "infusion4.png"), false));
        levelingPages.add(new PageTextImage(translateComponent(base + "leveling.infusion.image5"), new ResourceLocation(IMAGE_BASE + "infusion5.png"), false));

        entries.put(new ResourceLocation(base + "leveling"), new EntryText(levelingPages, translateComponent(base + "leveling")));


        List<IPage> skillPages = new ArrayList<>();
        skillPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "skills.text", UtilLib.translate(ModKeys.getKeyBinding(ModKeys.KEY.SKILL).getTranslationKey()))));
        skillPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "skills.actions", UtilLib.translate(ModKeys.getKeyBinding(ModKeys.KEY.ACTION).getTranslationKey()))));
        skillPages.addAll(PageHelper.pagesForLongText(translateComponent("guide.vampirism.skills.bind_action")));
        skillPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "skills.actions2")));

        entries.put(new ResourceLocation(base + "skills"), new EntryText(skillPages, translateComponent(base + "skills")));

        List<IPage> lordPages = new ArrayList<>();
        lordPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "lord.text", ModEntities.task_master_vampire.getName().getString(), VReference.VAMPIRE_FACTION.getLordTitle(1, false).getString(), VReference.VAMPIRE_FACTION.getLordTitle(1, true).getString(), VReference.VAMPIRE_FACTION.getLordTitle(VReference.VAMPIRE_FACTION.getHighestLordLevel(), false).getString(), VReference.VAMPIRE_FACTION.getLordTitle(VReference.VAMPIRE_FACTION.getHighestLordLevel(), true).getString())), new ResourceLocation("guide.vampirism.entity.taskmaster")));
        PageTable.Builder lordTitleBuilder = new PageTable.Builder(3).setHeadline(translateComponent(base + "lord.titles"));
        lordTitleBuilder.addUnlocLine("text.vampirism.level", "text.vampirism.title", "text.vampirism.title");
        lordTitleBuilder.addLine(1, VReference.VAMPIRE_FACTION.getLordTitle(1, false).getString(), VReference.VAMPIRE_FACTION.getLordTitle(1, true).getString());
        lordTitleBuilder.addLine(2, VReference.VAMPIRE_FACTION.getLordTitle(2, false).getString(), VReference.VAMPIRE_FACTION.getLordTitle(2, true).getString());
        lordTitleBuilder.addLine(3, VReference.VAMPIRE_FACTION.getLordTitle(3, false).getString(), VReference.VAMPIRE_FACTION.getLordTitle(3, true).getString());
        lordTitleBuilder.addLine(4, VReference.VAMPIRE_FACTION.getLordTitle(4, false).getString(), VReference.VAMPIRE_FACTION.getLordTitle(4, true).getString());
        lordTitleBuilder.addLine(5, VReference.VAMPIRE_FACTION.getLordTitle(5, false).getString(), VReference.VAMPIRE_FACTION.getLordTitle(5, true).getString());
        lordPages.add(lordTitleBuilder.build());
        lordPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "lord.minion", loc(ModItems.vampire_minion_binding), loc(ModItems.vampire_minion_upgrade_simple), loc(ModItems.vampire_minion_upgrade_enhanced), loc(ModItems.vampire_minion_upgrade_special))), new ResourceLocation("guide.vampirism.items.vampire_minion_binding")));
        lordPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent("guide.vampirism.common.minion_control", translate(ModKeys.getKeyBinding(ModKeys.KEY.MINION).getTranslationKey()), translate("text.vampirism.minion.call_single"), translate("text.vampirism.minion.respawn")))));
        entries.put(new ResourceLocation(base + "lord"), new EntryText(lordPages, new TranslationTextComponent(base + "lord")));

        List<IPage> unvampirePages = new ArrayList<>();
        unvampirePages.addAll(PageHelper.pagesForLongText(translateComponent(base + "unvampire.text", loc(ModBlocks.church_altar))));
        entries.put(new ResourceLocation(base + "unvampire"), new EntryText(unvampirePages, translateComponent(base + "unvampire")));

        links.putAll(entries);
        return entries;
    }

    private static Map<ResourceLocation, EntryAbstract> buildHunter() {
        Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
        String base = "guide.vampirism.hunter.";

        List<IPage> gettingStarted = new ArrayList<>();
        ITextComponent become = translateComponent(base + "getting_started.become", translateComponent(ModEntities.hunter_trainer.getTranslationKey()), loc(ModItems.injection_garlic));
        gettingStarted.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(become), new ResourceLocation("guide.vampirism.items.injection_empty")));
        gettingStarted.add(new PageImage(new ResourceLocation(IMAGE_BASE + "hunter_trainer.png")));
        gettingStarted.addAll(PageHelper.pagesForLongText(translateComponent(base + "getting_started.as_hunter")));
        entries.put(new ResourceLocation(base + "getting_started"), new EntryText(gettingStarted, translateComponent(base + "getting_started")));

        HunterLevelingConf levelingConf = HunterLevelingConf.instance();
        List<IPage> levelingPages = new ArrayList<>();
        levelingPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "leveling.intro")));
        String train1 = "§l" + translate(base + "leveling.to_reach", "2-4") + "§r\n";
        train1 += translate(base + "leveling.train1.text", levelingConf.getVampireBloodCountForBasicHunter(2), levelingConf.getVampireBloodCountForBasicHunter(3), levelingConf.getVampireBloodCountForBasicHunter(4));
        levelingPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new StringTextComponent(train1)), new ResourceLocation("guide.vampirism.items.stake"), new ResourceLocation("guide.vampirism.items.vampire_blood_bottle")));

        String train2 = "§l" + translate(base + "leveling.to_reach", "5+") + "§r\n";
        train2 += translate(base + "leveling.train2.text", loc(ModBlocks.hunter_table), loc(ModBlocks.weapon_table), loc(ModBlocks.potion_table), loc(ModBlocks.alchemical_cauldron));
        levelingPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new TranslationTextComponent(train2)), new ResourceLocation("guide.vampirism.blocks.hunter_table"), new ResourceLocation("guide.vampirism.blocks.weapon_table"), new ResourceLocation("guide.vampirism.blocks.alchemical_cauldron"), new ResourceLocation("guide.vampirism.blocks.potion_table")));
        PageTable.Builder builder = new PageTable.Builder(4);
        builder.addUnlocLine("text.vampirism.level", base + "leveling.train2.fang", loc(ModItems.pure_blood_0), loc(ModItems.vampire_book));
        for (int i = levelingConf.TABLE_MIN_LEVEL; i <= levelingConf.TABLE_MAX_LEVEL; i++) {
            int[] req = levelingConf.getItemRequirementsForTable(i);
            String pure = "";
            if (req[1] > 0) {
                pure = "" + req[1] + " Purity(" + (req[2] + 1) + ")";
            }
            builder.addLine(i, req[0], pure, req[3]);
        }

        builder.setHeadline(translateComponent(base + "leveling.train2.req"));
        PageHolderWithLinks requirementsTable = new PageHolderWithLinks(builder.build());
        requirementsTable.addLink(new ResourceLocation("guide.vampirism.items.vampire_fang"));
        requirementsTable.addLink(new ResourceLocation("guide.vampirism.items.pure_blood_0"));
        requirementsTable.addLink(new ResourceLocation("guide.vampirism.items.vampire_book"));
        levelingPages.add(requirementsTable);

        entries.put(new ResourceLocation(base + "leveling"), new EntryText(levelingPages, translateComponent(base + "leveling")));

        List<IPage> skillPages = new ArrayList<>();
        skillPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "skills.intro", translate(ModKeys.getKeyBinding(ModKeys.KEY.SKILL).getTranslationKey()))));
        String disguise = String.format("§l%s§r\n", HunterActions.disguise_hunter.getName().getString());
        disguise += translate(base + "skills.disguise.text", ModKeys.getKeyBinding(ModKeys.KEY.ACTION).getTranslationKey());
        skillPages.addAll(PageHelper.pagesForLongText(new StringTextComponent(disguise)));
        String weaponTable = String.format("§l%s§r\n", loc(ModBlocks.weapon_table));
        weaponTable += translate(base + "skills.weapon_table.text");
        skillPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new StringTextComponent(weaponTable)), new ResourceLocation("guide.vampirism.blocks.weapon_table")));
        entries.put(new ResourceLocation(base + "skills"), new EntryText(skillPages, translateComponent(base + "skills")));
        String potionTable = String.format("§l%s§r\n", loc(ModBlocks.potion_table));
        potionTable += translate(base + "skills.potion_table.text");
        List<IPage> potionTablePages = new ArrayList<>(PageHelper.pagesForLongText(new StringTextComponent(potionTable)));
        potionTablePages.addAll(Arrays.asList(generatePotionMixes()));
        skillPages.addAll(GuideHelper.addLinks(potionTablePages, new ResourceLocation("guide.vampirism.blocks.potion_table")));
        entries.put(new ResourceLocation(base + "skills"), new EntryText(skillPages, new TranslationTextComponent(base + "skills")));

        List<IPage> vampSlayerPages = new ArrayList<>();
        vampSlayerPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "vamp_slayer.intro")));
        String garlic = String.format("§l%s§r\n", loc(ModItems.item_garlic));
        garlic += translate(base + "vamp_slayer.garlic") + "\n" + translate(base + "vamp_slayer.garlic2") + "\n" + translate(base + "vamp_slayer.garlic.diffusor");
        vampSlayerPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new StringTextComponent(garlic)), new ResourceLocation("guide.vampirism.blocks.garlic_beacon_normal")));
        String holyWater = String.format("§l%s§r\n", loc(ModItems.holy_water_bottle_normal));
        holyWater += translate(base + "vamp_slayer.holy_water");
        vampSlayerPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new StringTextComponent(holyWater)), new ResourceLocation("guide.vampirism.items.holy_water_bottle")));
        String fire = String.format("§l%s§r\n", loc(Blocks.FIRE));
        fire += translate(base + "vamp_slayer.fire");
        vampSlayerPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(new StringTextComponent(fire)), new ResourceLocation("guide.vampirism.items.item_alchemical_fire"), new ResourceLocation("guide.vampirism.items.crossbow_arrow_normal")));
        entries.put(new ResourceLocation(base + "vamp_slayer"), new EntryText(vampSlayerPages, translateComponent(base + "vamp_slayer")));

        List<IPage> lordPages = new ArrayList<>();
        lordPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "lord.text", ModEntities.task_master_hunter.getName().getString(), VReference.HUNTER_FACTION.getLordTitle(1, false).getString(), VReference.HUNTER_FACTION.getLordTitle(VReference.HUNTER_FACTION.getHighestLordLevel(), false).getString())), new ResourceLocation("guide.vampirism.entity.taskmaster")));
        PageTable.Builder lordTitleBuilder = new PageTable.Builder(2);
        lordTitleBuilder.setHeadline(translateComponent(base + "lord.titles"));
        lordTitleBuilder.addUnlocLine("text.vampirism.level", "text.vampirism.title");
        lordTitleBuilder.addLine(1, VReference.HUNTER_FACTION.getLordTitle(1, false).getString());
        lordTitleBuilder.addLine(2, VReference.HUNTER_FACTION.getLordTitle(2, false).getString());
        lordTitleBuilder.addLine(3, VReference.HUNTER_FACTION.getLordTitle(3, false).getString());
        lordTitleBuilder.addLine(4, VReference.HUNTER_FACTION.getLordTitle(4, false).getString());
        lordTitleBuilder.addLine(5, VReference.HUNTER_FACTION.getLordTitle(5, false).getString());
        lordPages.add(lordTitleBuilder.build());
        lordPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "lord.minion", loc(ModItems.hunter_minion_equipment), loc(ModItems.hunter_minion_upgrade_simple), loc(ModItems.hunter_minion_upgrade_enhanced), loc(ModItems.hunter_minion_upgrade_special))), new ResourceLocation("guide.vampirism.items.hunter_minion_equipment")));
        lordPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent("guide.vampirism.common.minion_control", translate(ModKeys.getKeyBinding(ModKeys.KEY.MINION).getTranslationKey()), translate("text.vampirism.minion.call_single"), translate("text.vampirism.minion.respawn")))));
        entries.put(new ResourceLocation(base + "lord"), new EntryText(lordPages, new TranslationTextComponent(base + "lord")));

        List<IPage> unHunterPages = new ArrayList<>();
        unHunterPages.addAll(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "unhunter.text", loc(ModItems.injection_sanguinare), loc(ModBlocks.med_chair))), new ResourceLocation("guide.vampirism.items.injection_empty"), new ResourceLocation("guide.vampirism.blocks.item_med_chair")));
        entries.put(new ResourceLocation(base + "unhunter"), new EntryText(unHunterPages, translateComponent(base + "unhunter")));

        links.putAll(entries);
        return entries;
    }

    private static Map<ResourceLocation, EntryAbstract> buildCreatures() {
        Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
        String base = "guide.vampirism.entity.";

        ArrayList<IPage> generalPages = new ArrayList<>(PageHelper.pagesForLongText(ITextProperties.func_240655_a_(translateComponent(base + "general.text"), translateComponent(base + "general.text2"))));
        entries.put(new ResourceLocation(base + "general"), new EntryText(generalPages, translateComponent(base + "general")));

        ArrayList<IPage> hunterPages = new ArrayList<>();
        hunterPages.add(new PageEntity((world) -> {
            BasicHunterEntity entity = ModEntities.hunter.create(world);
            entity.setLevel(1);
            return entity;
        }));
        hunterPages.add(new PageEntity((world) -> {
            BasicHunterEntity entity = ModEntities.hunter.create(world);
            entity.setLevel(0);
            return entity;
        }));
        hunterPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "hunter.text", loc(ModItems.human_heart))));
        entries.put(new ResourceLocation(base + "hunter"), new EntryText(hunterPages, ModEntities.hunter.getName()));

        ArrayList<IPage> vampirePages = new ArrayList<>();
        vampirePages.add(new PageEntity(ModEntities.vampire));
        vampirePages.addAll(PageHelper.pagesForLongText(translateComponent(base + "vampire.text", loc(ModItems.vampire_fang), loc(ModItems.vampire_blood_bottle), loc(ModItems.stake))));
        entries.put(new ResourceLocation(base + "vampire"), new EntryText(vampirePages, ModEntities.vampire.getName()));

        ArrayList<IPage> advancedHunterPages = new ArrayList<>();
        advancedHunterPages.add(new PageEntity(ModEntities.advanced_hunter));
        advancedHunterPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "advanced_hunter.text")));
        entries.put(new ResourceLocation(base + "advanced_hunter"), new EntryText(advancedHunterPages, ModEntities.advanced_hunter.getName()));

        ArrayList<IPage> advancedVampirePages = new ArrayList<>();
        advancedVampirePages.add(new PageEntity(ModEntities.advanced_vampire));
        advancedVampirePages.addAll(PageHelper.pagesForLongText(translateComponent(base + "advanced_vampire.text", loc(ModItems.blood_bottle), loc(ModItems.vampire_blood_bottle))));
        entries.put(new ResourceLocation(base + "advanced_vampire"), new EntryText(advancedVampirePages, ModEntities.advanced_vampire.getName()));

        ArrayList<IPage> vampireBaronPages = new ArrayList<>();
        vampireBaronPages.add(new PageEntity(ModEntities.vampire_baron));
        vampireBaronPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "vampire_baron.text", loc(ModItems.pure_blood_0))));
        GuideHelper.addLinks(vampireBaronPages, new ResourceLocation("guide.vampirism.world.vampire_forest"));
        entries.put(new ResourceLocation(base + "vampire_baron"), new EntryText(vampireBaronPages, ModEntities.vampire_baron.getName()));

        ArrayList<IPage> hunterTrainerPages = new ArrayList<>();
        hunterTrainerPages.add(new PageEntity(ModEntities.hunter_trainer));
        hunterTrainerPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "hunter_trainer.text")));
        entries.put(new ResourceLocation(base + "hunter_trainer"), new EntryText(hunterTrainerPages, ModEntities.hunter_trainer.getName()));

        ArrayList<IPage> taskMasterPages = new ArrayList<>();
        taskMasterPages.add(new PageEntity(ModEntities.task_master_vampire));
        taskMasterPages.add(new PageEntity(ModEntities.task_master_hunter));
        taskMasterPages.addAll(PageHelper.pagesForLongText(translateComponent(base + "taskmaster.text")));
        taskMasterPages.add(new PageImage(new ResourceLocation(IMAGE_BASE + "taskscreen.png")));
        entries.put(new ResourceLocation(base + "taskmaster"), new EntryText(taskMasterPages, new TranslationTextComponent(base + "taskmaster")));




        links.putAll(entries);
        return entries;
    }

    private static Map<ResourceLocation, EntryAbstract> buildWorld() {
        Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
        String base = "guide.vampirism.world.";

        List<IPage> vampireForestPages = new ArrayList<>(PageHelper.pagesForLongText(translateComponent(base + "vampire_forest.text")));
        entries.put(new ResourceLocation(base + "vampire_forest"), new EntryText(vampireForestPages, translateComponent(base + "vampire_forest")));

        List<IPage> villagePages = new ArrayList<>(GuideHelper.addLinks(PageHelper.pagesForLongText(translateComponent(base + "villages.text")), new ResourceLocation("guide.vampirism.blocks.totem_base"), new ResourceLocation("guide.vampirism.blocks.totem_top_crafted")));
        entries.put(new ResourceLocation(base + "villages"), new EntryText(villagePages, translateComponent(base + "villages")));


        links.putAll(entries);
        return entries;
    }

    private static Map<ResourceLocation, EntryAbstract> buildItems() {
        Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
        String base = "guide.vampirism.items.";
        //General
        ItemInfoBuilder.create(ModItems.vampire_fang).build(entries);
        ItemInfoBuilder.create(ModItems.human_heart).build(entries);
        ItemInfoBuilder.create(ModItems.pure_blood_0, ModItems.pure_blood_1, ModItems.pure_blood_2, ModItems.pure_blood_3, ModItems.pure_blood_4).setFormats(translateComponent(ModEntities.vampire_baron.getTranslationKey())).build(entries);
        ItemInfoBuilder.create(ModItems.vampire_blood_bottle).setFormats(translateComponent(ModEntities.vampire.getTranslationKey()), loc(ModItems.stake), translateComponent(ModEntities.advanced_vampire.getTranslationKey())).build(entries);
        ItemInfoBuilder.create(ModItems.vampire_book).build(entries);
        //Vampire
        ItemInfoBuilder.create(false, BloodBottleItem.getStackWithDamage(BloodBottleItem.AMOUNT)).build(entries);
        ItemInfoBuilder.create(ModItems.blood_infused_iron_ingot).recipes("vampire/blood_infused_iron_ingot", "vampire/blood_infused_enhanced_iron_ingot").build(entries);
        ItemInfoBuilder.create(ModItems.heart_seeker_normal, ModItems.heart_seeker_enhanced, ModItems.heart_seeker_ultimate).recipes("vampire/heart_seeker_normal", "vampire/heart_seeker_enhanced").build(entries);
        ItemInfoBuilder.create(ModItems.heart_striker_normal, ModItems.heart_striker_enhanced, ModItems.heart_striker_ultimate).recipes("vampire/heart_striker_normal", "vampire/heart_striker_normal").build(entries);
        ItemInfoBuilder.create(ModItems.vampire_cloak_black_red, ModItems.vampire_cloak_black_blue, ModItems.vampire_cloak_red_black, ModItems.vampire_cloak_black_white, ModItems.vampire_cloak_white_black).recipes("vampire/vampire_cloak_black_red", "vampire/vampire_cloak_black_blue", "vampire/vampire_cloak_black_white", "vampire/vampire_cloak_red_black", "vampire/vampire_cloak_white_black").build(entries);
        ItemInfoBuilder.create(ModItems.feeding_adapter).customPages(GuideHelper.createItemTaskDescription(ModTasks.feeding_adapter)).build(entries);
        ItemInfoBuilder.create(ModItems.vampire_minion_binding, ModItems.vampire_minion_upgrade_simple, ModItems.vampire_minion_upgrade_enhanced, ModItems.vampire_minion_upgrade_special).setFormats(loc(ModItems.vampire_minion_binding), loc(ModItems.vampire_minion_upgrade_simple), ModItems.vampire_minion_upgrade_simple.getMinLevel() + 1, ModItems.vampire_minion_upgrade_simple.getMaxLevel() + 1, loc(ModItems.vampire_minion_upgrade_enhanced), ModItems.vampire_minion_upgrade_enhanced.getMinLevel() + 1, ModItems.vampire_minion_upgrade_enhanced.getMaxLevel() + 1, loc(ModItems.vampire_minion_upgrade_special), ModItems.vampire_minion_upgrade_special.getMinLevel() + 1, ModItems.vampire_minion_upgrade_special.getMaxLevel() + 1, translate(ModEntities.task_master_vampire.getTranslationKey())).setLinks(new ResourceLocation("guide.vampirism.entity.taskmaster"), new ResourceLocation("guide.vampirism.vampire.lord")).build(entries);
        //Hunter
        ItemInfoBuilder.create(ModItems.injection_empty, ModItems.injection_garlic, ModItems.injection_sanguinare).recipes("general/injection_0", "general/injection_1", "general/injection_2").build(entries);
        ItemInfoBuilder.create(ModItems.hunter_intel_0).setLinks(new ResourceLocation("guide.vampirism.blocks.hunter_table")).setFormats(loc(ModBlocks.hunter_table)).build(entries);
        ItemInfoBuilder.create(ModItems.item_garlic).build(entries);
        ItemInfoBuilder.create(ModItems.purified_garlic).setFormats(loc(ModBlocks.garlic_beacon_normal)).setLinks(new ResourceLocation("guide.vampirism.blocks.garlic_beacon_normal")).recipes("alchemical_cauldron/purified_garlic").build(entries);
        ItemInfoBuilder.create(ModItems.pitchfork).recipes("weapontable/pitchfork").build(entries);
        ItemInfoBuilder.create(ModItems.stake).setFormats(((int) (VampirismConfig.BALANCE.hsInstantKill1MaxHealth.get() * 100)) + "%").recipes("hunter/stake").build(entries);
        ItemInfoBuilder.create(ModItems.basic_crossbow, ModItems.enhanced_crossbow, ModItems.basic_double_crossbow, ModItems.enhanced_double_crossbow, ModItems.basic_tech_crossbow, ModItems.enhanced_tech_crossbow).setFormats(loc(ModItems.crossbow_arrow_normal), loc(ModItems.tech_crossbow_ammo_package)).setLinks(new ResourceLocation("guide.vampirism.items.crossbow_arrow_normal")).recipes("weapontable/basic_crossbow", "weapontable/enhanced_crossbow", "weapontable/basic_double_crossbow", "weapontable/enhanced_double_crossbow", "weapontable/basic_tech_crossbow", "weapontable/enhanced_tech_crossbow", "weapontable/tech_crossbow_ammo_package").setName("crossbows").customName().build(entries);
        ItemInfoBuilder.create(ModItems.crossbow_arrow_normal, ModItems.crossbow_arrow_spitfire, ModItems.crossbow_arrow_vampire_killer).recipes("hunter/crossbow_arrow_normal", "weapontable/crossbow_arrow_spitfire", "weapontable/crossbow_arrow_vampire_killer").build(entries);
        ItemInfoBuilder.create(ModItems.holy_water_bottle_normal, ModItems.holy_water_bottle_enhanced, ModItems.holy_water_bottle_ultimate).setLinks(new ResourceLocation("guide.vampirism.hunter.vamp_slayer"), new ResourceLocation("guide.vampirism.items.holy_salt")).setFormats(loc(ModItems.holy_salt_water), loc(ModItems.holy_salt_water), loc(ModItems.holy_salt)).brewingItems(ModItems.holy_salt_water, ModItems.holy_water_splash_bottle_normal).build(entries);
        ItemInfoBuilder.create(ModItems.holy_salt).setLinks(new ResourceLocation("guide.vampirism.items.holy_water_bottle")).setFormats(loc(ModItems.pure_salt), loc(ModItems.pure_salt), loc(ModBlocks.alchemical_cauldron)).recipes("alchemical_cauldron/pure_salt").build(entries);
        ItemInfoBuilder.create(ModItems.item_alchemical_fire).setLinks(new ResourceLocation("guide.vampirism.items.crossbow_arrow_normal")).recipes("alchemical_cauldron/alchemical_fire_4", "alchemical_cauldron/alchemical_fire_5", "alchemical_cauldron/alchemical_fire_6").build(entries);
        ItemInfoBuilder.create(ModItems.armor_of_swiftness_chest_normal, ModItems.armor_of_swiftness_chest_enhanced, ModItems.armor_of_swiftness_chest_enhanced, ModItems.armor_of_swiftness_legs_normal, ModItems.armor_of_swiftness_legs_enhanced, ModItems.armor_of_swiftness_legs_ultimate, ModItems.armor_of_swiftness_head_normal, ModItems.armor_of_swiftness_head_enhanced, ModItems.armor_of_swiftness_head_ultimate, ModItems.armor_of_swiftness_feet_normal, ModItems.armor_of_swiftness_feet_enhanced, ModItems.armor_of_swiftness_feet_ultimate).recipes("weapontable/armor_of_swiftness_chest_normal", "weapontable/armor_of_swiftness_legs_normal", "weapontable/armor_of_swiftness_head_normal", "weapontable/armor_of_swiftness_feet_normal", "weapontable/armor_of_swiftness_chest_enhanced", "weapontable/armor_of_swiftness_legs_enhanced", "weapontable/armor_of_swiftness_head_enhanced", "weapontable/armor_of_swiftness_feet_enhanced").build(entries);
        ItemInfoBuilder.create(ModItems.hunter_coat_chest_normal, ModItems.hunter_coat_chest_enhanced, ModItems.hunter_coat_chest_enhanced, ModItems.hunter_coat_legs_normal, ModItems.hunter_coat_legs_enhanced, ModItems.hunter_coat_legs_ultimate, ModItems.hunter_coat_head_normal, ModItems.hunter_coat_head_enhanced, ModItems.hunter_coat_head_ultimate, ModItems.hunter_coat_feet_normal, ModItems.hunter_coat_feet_enhanced, ModItems.hunter_coat_feet_ultimate).recipes("weapontable/hunter_coat_chest_normal", "weapontable/hunter_coat_legs_normal", "weapontable/hunter_coat_head_normal", "weapontable/hunter_coat_feet_normal", "weapontable/hunter_coat_chest_enhanced", "weapontable/hunter_coat_legs_enhanced", "weapontable/hunter_coat_head_enhanced", "weapontable/hunter_coat_feet_enhanced").build(entries);
        ItemInfoBuilder.create(ModItems.obsidian_armor_chest_normal, ModItems.obsidian_armor_chest_enhanced, ModItems.obsidian_armor_chest_enhanced, ModItems.obsidian_armor_legs_normal, ModItems.obsidian_armor_legs_enhanced, ModItems.obsidian_armor_legs_ultimate, ModItems.obsidian_armor_head_normal, ModItems.obsidian_armor_head_enhanced, ModItems.obsidian_armor_head_ultimate, ModItems.obsidian_armor_feet_normal, ModItems.obsidian_armor_feet_enhanced, ModItems.obsidian_armor_feet_ultimate).recipes("weapontable/obsidian_armor_chest_normal", "weapontable/obsidian_armor_legs_normal", "weapontable/obsidian_armor_head_normal", "weapontable/obsidian_armor_feet_normal", "weapontable/obsidian_armor_chest_enhanced", "weapontable/obsidian_armor_legs_enhanced", "weapontable/obsidian_armor_head_enhanced", "weapontable/obsidian_armor_feet_enhanced").build(entries);
        ItemInfoBuilder.create(ModItems.hunter_axe_normal, ModItems.hunter_axe_enhanced, ModItems.hunter_axe_ultimate).recipes("weapontable/hunter_axe_normal", "weapontable/hunter_axe_enhanced").build(entries);
        ItemInfoBuilder.create(ModItems.hunter_minion_equipment, ModItems.hunter_minion_upgrade_simple, ModItems.hunter_minion_upgrade_enhanced, ModItems.hunter_minion_upgrade_special).setFormats(loc(ModItems.hunter_minion_equipment), loc(ModItems.hunter_minion_upgrade_simple), ModItems.hunter_minion_upgrade_simple.getMinLevel() + 1, ModItems.hunter_minion_upgrade_simple.getMaxLevel() + 1, loc(ModItems.hunter_minion_upgrade_enhanced), ModItems.hunter_minion_upgrade_enhanced.getMinLevel() + 1, ModItems.hunter_minion_upgrade_enhanced.getMaxLevel() + 1, loc(ModItems.hunter_minion_upgrade_special), ModItems.hunter_minion_upgrade_special.getMinLevel() + 1, ModItems.hunter_minion_upgrade_special.getMaxLevel() + 1, translate(ModEntities.task_master_hunter.getTranslationKey())).setLinks(new ResourceLocation("guide.vampirism.entity.taskmaster"), new ResourceLocation("guide.vampirism.hunter.lord")).build(entries);
        links.putAll(entries);
        return entries;
    }

    private static Map<ResourceLocation, EntryAbstract> buildBlocks() {
        Map<ResourceLocation, EntryAbstract> entries = new LinkedHashMap<>();
        String base = "guide.vampirism.blocks.";
        //General
        ItemInfoBuilder.create(ModBlocks.castle_block_dark_brick).recipes("general/castle_block_dark_brick_0", "general/castle_block_dark_brick_1", "general/castle_block_dark_stone", "general/castle_block_normal_brick", "general/castle_block_purple_brick", "general/castle_slab_dark_brick", "general/castle_stairs_dark_brick").build(entries);
        ItemInfoBuilder.create(ModBlocks.vampire_orchid).build(entries);
        //Vampire
        ItemInfoBuilder.create(ModBlocks.blood_container).recipes("vampire/blood_container").build(entries);
        ItemInfoBuilder.create(ModBlocks.altar_inspiration).setLinks(new ResourceLocation("guide.vampirism.vampire.leveling")).recipes("vampire/altar_inspiration").build(entries);
        ItemInfoBuilder.create(ModBlocks.altar_infusion).setLinks(new ResourceLocation("guide.vampirism.vampire.leveling")).recipes("vampire/altar_infusion", "vampire/altar_pillar", "vampire/altar_tip").build(entries);
        ItemInfoBuilder.create(ModBlocks.coffin).recipes("vampire/coffin").build(entries);
        ItemInfoBuilder.create(ModBlocks.church_altar).build(entries);
        //Hunter
        ItemInfoBuilder.create(true, new ItemStack(ModItems.item_med_chair)).setFormats(loc(ModItems.injection_garlic), loc(ModItems.injection_sanguinare)).recipes("hunter/item_med_chair").build(entries);
        ItemInfoBuilder.create(ModBlocks.hunter_table).setFormats(loc(ModItems.hunter_intel_0)).setLinks(new ResourceLocation("guide.vampirism.hunter.leveling"), new ResourceLocation("guide.vampirism.items.hunter_intel")).recipes("hunter/hunter_table").build(entries);
        ItemInfoBuilder.create(ModBlocks.weapon_table).recipes("hunter/weapon_table").build(entries);
        ItemInfoBuilder.create(ModBlocks.alchemical_cauldron).recipes("hunter/alchemical_cauldron").build(entries);
        int cn = VampirismConfig.BALANCE.hsGarlicDiffusorNormalDist.get() * 2 + 1;
        int ce = VampirismConfig.BALANCE.hsGarlicDiffusorEnhancedDist.get() * 2 + 1;
        ItemInfoBuilder.create(ModBlocks.garlic_beacon_normal, ModBlocks.garlic_beacon_weak, ModBlocks.garlic_beacon_weak).setFormats(cn, cn, ce, ce, loc(ModItems.purified_garlic)).setLinks(new ResourceLocation("guide.vampirism.items.item_garlic"), new ResourceLocation("guide.vampirism.items.purified_garlic"), new ResourceLocation("guide.vampirism.items.holy_water_bottle")).recipes("hunter/garlic_beacon", "hunter/garlic_beacon_improved", "alchemical_cauldron/garlic_beacon_core", "alchemical_cauldron/garlic_beacon_core_improved").build(entries);
        ItemInfoBuilder.create(ModBlocks.blood_pedestal).recipes("vampire/blood_pedestal").build(entries);
        ItemInfoBuilder.create(ModBlocks.blood_grinder).recipes("general/blood_grinder").setFormats(loc(ModItems.human_heart), loc(Items.BEEF), loc(ModBlocks.blood_sieve)).build(entries);
        ItemInfoBuilder.create(ModBlocks.blood_sieve).recipes("general/blood_sieve").setFormats(translateComponent(ModFluids.impure_blood.getAttributes().getTranslationKey()), loc(ModBlocks.blood_grinder)).setLinks(new ResourceLocation("guide.vampirism.blocks.blood_grinder")).build(entries);
        ItemInfoBuilder.create(ModBlocks.totem_top_crafted, ModBlocks.totem_top).setLinks(new ResourceLocation("guide.vampirism.blocks.totem_base"), new ResourceLocation("guide.vampirism.world.villages")).build(entries);
        ItemInfoBuilder.create(ModBlocks.totem_base).recipes("general/totem_base").setLinks(new ResourceLocation("guide.vampirism.blocks.totem_top_crafted"), new ResourceLocation("guide.vampirism.world.villages")).build(entries);
        ItemInfoBuilder.create(ModBlocks.potion_table).recipes("hunter/potion_table").customPages(generatePotionMixes()).build(entries);
        links.putAll(entries);
        return entries;
    }

    private static IPage[] generatePotionMixes() {
        IPage[] pages = new IPage[6];
        pages[0] = new PagePotionTableMix(HunterSkills.durable_brewing.getName(), VampirismAPI.extendedBrewingRecipeRegistry().getPotionMixes().stream().filter(mix -> mix.durable && !mix.concentrated && !mix.efficient).toArray(ExtendedPotionMix[]::new));
        pages[1] = new PagePotionTableMix(HunterSkills.concentrated_brewing.getName(), VampirismAPI.extendedBrewingRecipeRegistry().getPotionMixes().stream().filter(mix -> mix.concentrated && !mix.durable && !mix.efficient).toArray(ExtendedPotionMix[]::new));
        pages[2] = new PagePotionTableMix(HunterSkills.durable_brewing.getName().deepCopy().appendString("\n").append(HunterSkills.efficient_brewing.getName()), VampirismAPI.extendedBrewingRecipeRegistry().getPotionMixes().stream().filter(mix -> mix.durable && !mix.concentrated && mix.efficient).toArray(ExtendedPotionMix[]::new));
        pages[3] = new PagePotionTableMix(HunterSkills.concentrated_brewing.getName().deepCopy().appendString("\n").append(HunterSkills.efficient_brewing.getName()), VampirismAPI.extendedBrewingRecipeRegistry().getPotionMixes().stream().filter(mix -> mix.concentrated && !mix.durable && mix.efficient).toArray(ExtendedPotionMix[]::new));
        pages[4] = new PagePotionTableMix(HunterSkills.master_brewer.getName(), VampirismAPI.extendedBrewingRecipeRegistry().getPotionMixes().stream().filter(mix -> mix.master && !mix.durable && !mix.concentrated && !mix.efficient).toArray(ExtendedPotionMix[]::new));
        pages[5] = new PagePotionTableMix(HunterSkills.master_brewer.getName().deepCopy().appendString("\n").append(HunterSkills.efficient_brewing.getName()), VampirismAPI.extendedBrewingRecipeRegistry().getPotionMixes().stream().filter(mix -> mix.master && !mix.durable && !mix.concentrated && mix.efficient).toArray(ExtendedPotionMix[]::new));
        return pages;
    }

    public static IFormattableTextComponent translateComponent(String key, Object... format) {
        String result = UtilLib.translate(key, format);
        return new StringTextComponent(result.replaceAll("\\\\n", Matcher.quoteReplacement("\n"))); //Fix legacy newlines. //Probably shouldn't use new StringTextComponent here, but don't want to rewrite everything
    }

    public static String translate(String key, Object... format) {
        String result = UtilLib.translate(key, format);
        return result.replaceAll("\\\\n", Matcher.quoteReplacement("\n"));
    }


    @Nullable
    @Override
    public Book buildBook() {
        BookBinder binder = new BookBinder(new ResourceLocation("vampirism", "guidebook"));
        binder.setGuideTitleKey("guide.vampirism.title");
        binder.setItemNameKey("guide.vampirism");
        binder.setHeaderKey("guide.vampirism.welcome");
        binder.setAuthor(new StringTextComponent("Maxanier"));
        binder.setColor(Color.WHITE);
        binder.setOutlineTexture(new ResourceLocation("vampirismguide", "textures/gui/book_violet_border.png"));
        binder.setSpawnWithBook();
        binder.setContentProvider(GuideBook::buildCategories);
        return guideBook = binder.build();
    }

    @Nullable
    @Override
    public ResourceLocation getModel() {
        return new ResourceLocation(REFERENCE.MODID, "guidebook");
    }
}
