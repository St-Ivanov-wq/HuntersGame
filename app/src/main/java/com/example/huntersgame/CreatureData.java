package com.example.huntersgame;

import java.util.ArrayList;
import java.util.List;

public class CreatureData {
    public static List<Creature> getCreatures() {
        List<Creature> creatures = new ArrayList<>();

        // Samodiva (Fairy)
        creatures.add(new Creature("1", "Samodiva", "Самодива",
                "Beautiful woodland fairies with magical powers. They live in forests and mountains, dancing in circles called 'horo'.",
                "Forests and mountains", 100, R.drawable.ic_samodiva));

        // Dragon (Zmey)
        creatures.add(new Creature("2", "Zmey", "Змей",
                "A dragon-like creature that protects villages but also falls in love with human women.",
                "Mountains and caves", 150, R.drawable.ic_dragon));

        // Karakondzul
        creatures.add(new Creature("3", "Karakondzul", "Караконджул",
                "A mischievous goblin that appears during the 12 days of Christmas. It rides on people's backs.",
                "Villages and forests", 80, R.drawable.ic_karakondzul));

        // add more creatures
        creatures.add(new Creature("4", "Torbalan", "Торбалан",
                "A vampire-like creature that rises from the dead to haunt the living.",
                "Graveyards and abandoned places", 120, R.drawable.ic_torbalan));

        creatures.add(new Creature("5", "Vila", "Вила",
                "Mountain spirits with long hair who can heal or harm humans.",
                "Mountain peaks and springs", 110, R.drawable.ic_vila));

        return creatures;
    }
}