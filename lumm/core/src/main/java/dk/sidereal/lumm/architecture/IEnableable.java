package dk.sidereal.lumm.architecture;

public interface IEnableable {

    public boolean isEnabled();

    public boolean isEnabledInHierarchy();

    public boolean setEnabled(boolean enabled);

}
