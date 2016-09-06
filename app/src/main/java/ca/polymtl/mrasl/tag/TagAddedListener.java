package ca.polymtl.mrasl.tag;

/**
 * This interface defines a callback when a new tag is added to a list of tag.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface TagAddedListener {

    /**
     * This method is called when a tag is added to a list of tag.
     *
     * @param tag The new tag
     */
    void onNewTag(Tag tag);

}
