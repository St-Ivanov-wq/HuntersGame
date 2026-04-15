package com.example.huntersgame;

import java.util.ArrayList;
import java.util.List;

public class CreatureData {
    public static List<Creature> getCreatures() {
        List<Creature> creatures = new ArrayList<>();

        // 1. Samodiva (Fairy)
        creatures.add(new Creature("1", "Samodiva", "Самодива",
                "Beautiful woodland fairies with magical powers. They live in forests and mountains, dancing in circles called 'horo'. They can heal or curse humans.",
                "Forests and mountains", 100, R.drawable.samodiva));

        // 2. Zmey (Dragon)
        creatures.add(new Creature("2", "Zmey", "Змей",
                "A dragon-like creature that protects villages but also falls in love with human women. It can fly and breathe fire.",
                "Mountains and caves", 150, R.drawable.zmey));

        // 3. Karakondzul (Goblin)
        creatures.add(new Creature("3", "Karakondzul", "Караконджул",
                "A mischievous goblin that appears during the 12 days of Christmas. It rides on people's backs and causes chaos.",
                "Villages and forests", 80, R.drawable.karakondzul));

        // 4. Torbalan (Vampire)
        creatures.add(new Creature("4", "Torbalan", "Торбалан",
                "A vampire-like creature that rises from the dead to haunt the living. It is said to suck blood and bring nightmares.",
                "Graveyards and abandoned places", 120, R.drawable.torbalan));

        // 5. Vila (Mountain Spirit)
        creatures.add(new Creature("5", "Vila", "Вила",
                "Mountain spirits with long hair who can heal or harm humans. They are known to dance in meadows and protect nature.",
                "Mountain peaks and springs", 110, R.drawable.vila));

        // 6. Poludnitsa (Noon Demon)
        creatures.add(new Creature("6", "Poludnitsa", "Полудница",
                "A noon demon that appears during the hottest hours of the day. She causes heatstroke and madness to those who work in the fields.",
                "Fields and meadows", 90, R.drawable.poludnitsa));

        // 7. Talasam (Water Spirit)
        creatures.add(new Creature("7", "Talasam", "Таласъм",
                "A water spirit that drowns people near rivers and lakes. It appears as a handsome man to lure victims.",
                "Rivers and lakes", 130, R.drawable.talasam));

        // 8. Lamia (Serpent Monster)
        creatures.add(new Creature("8", "Lamia", "Ламия",
                "A multi-headed serpent monster that terrorizes villages. It is known to kidnap young maidens and guard treasures.",
                "Caves and mountains", 180, R.drawable.lamia));

//        // 9. Baba Yaga (Forest Witch)
//        creatures.add(new Creature("9", "Baba Yaga", "Баба Яга",
//                "An old forest witch who lures children into the woods. She possesses dark magic and can transform into animals.",
//                "Deep forests", 140, R.drawable.babayaga));

        // 10. Orisnizi
        creatures.add(new Creature("10", "Orisnizi", "Орисници",
                "A trio of female creatures that looked like women. It is said that they go into homes where there’s a newborn, and when the baby is three days old, they would predict its fate.",
                "Abandoned places", 95, R.drawable.orisnizi));

        // 11. Hala (Storm Demon)
        creatures.add(new Creature("11", "Hala", "Хала",
                "A female demonic creature which is considered good by some, and evil by others. She would sometimes cast hails and storms on crops and destroy them.",
                "Mountains and springs", 160, R.drawable.hala));

        // 12. Borislav (Forest Guardian)
        creatures.add(new Creature("12", "Borislav", "Борислав",
                "A noble forest guardian who protects animals and travelers. He appears as a glowing figure in the woods.",
                "Forest clearings", 125, R.drawable.borislav));

        // 13.Chuma(Plague)
        creatures.add(new Creature("13", "Chuma", "Чума",
                "It is represented as an ugly old woman with long arms and bony hands. It only appeared in places where there were lots of sinners, killing them with arrows.",
                "Dark forests", 85, R.drawable.chuma));

        // 14. Drekavac (Screamer)
        creatures.add(new Creature("14", "Drekavac", "Дрекавац",
                "A creature that screams loudly to frighten people. It is born from the spirit of a deceased child.",
                "Swamps and forests", 105, R.drawable.drekavac));

        // 15. Rusalka (Water Nymph)
        creatures.add(new Creature("15", "Rusalka", "Русалка",
                "A water nymph with long green hair. She lures men into the water with her enchanting voice.",
                "Lakes and rivers", 115, R.drawable.rusalka));

        return creatures;
    }
}