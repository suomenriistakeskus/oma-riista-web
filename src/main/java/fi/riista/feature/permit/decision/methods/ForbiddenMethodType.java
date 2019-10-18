package fi.riista.feature.permit.decision.methods;

public enum ForbiddenMethodType {
    // Habides
    SNARES("snares"),
    LIVE_ANIMAL_DECOY("live animals used as decoys which are blind or mutilated"),
    TAPE_RECORDERS("tape recorders"),
    ELECTRICAL_DEVICE("electrical devices capable of killing and stunning"),
    ARTIFICIAL_LIGHT("artificial light sources"),
    MIRRORS("mirrors and other dazzling devices"),
    ILLUMINATION_DEVICE("devices for illuminating targets"),
    NIGHT_SHOOTING_DEVICE("sighting devices for night shooting comprising an electronic image magnifier or image converter"),
    EXPLOSIVES("explosives"),
    NETS("nets"),
    TRAPS("traps"),
    POISON("poison and poisoned or anaesthetic bait"),
    GASSING("gassing or smoking out"),
    AUTOMATIC_WEAPON("semi-automatic or automatic weapons with a magazine capable of holding more than two rounds of ammunition"),
    LIMES("limes"),
    HOOKS("hooks"),
    CROSSBOWS("crossbows for mammal"),

    // Metsästyslaki
    SPEAR("keihäs, vipukeihäs, harppuuna tai muu niitä vastaava ase"),
    BLOWPIPE("puhallusputki"),
    LEGHOLD_TRAP("raudat, jotka eivät tapa heti"),
    CONCEALED_WEAPON("haudat ja ansat, joihin on sijoitettu ampuma-ase tai keihäs"),

    // Habides other
    OTHER_SELECTIVE("any other selective mean arrangement or method"),
    OTHER_NON_SELECTIVE("any other non-selective/indiscriminate mean, arrangement or method capable of causing local disappearance of, or serious disturbance to, populations of the species");

    private final String name;

    ForbiddenMethodType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
