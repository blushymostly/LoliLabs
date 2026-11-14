package tmr.lolilabs.api;

/** Machine kind taxonomy. Wire code is the synced container index value. */
public enum MachineKind {

    SOLAR(0),
    UNIFIER(1),
    MATTER(2);

    private final int wireCode;

    MachineKind(int wireCode) {
        this.wireCode = wireCode;
    }

    public int wireCode() {
        return wireCode;
    }

    public boolean isSolar() {
        return this == SOLAR;
    }

    public boolean isUnifier() {
        return this == UNIFIER;
    }

    public boolean isMatter() {
        return this == MATTER;
    }

    public boolean usesWorkingLight() {
        return this == SOLAR || this == UNIFIER;
    }

    /** Matter keeps the clicked face; panels/unifier face the player. */
    public boolean facesClickedSide() {
        return this == MATTER;
    }
}
