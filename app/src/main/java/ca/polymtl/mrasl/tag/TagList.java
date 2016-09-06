package ca.polymtl.mrasl.tag;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import ca.polymtl.mrasl.ros.RosConnection;
import ca.polymtl.mrasl.ros.TagSurvivorSubscriberNode;
import ca.polymtl.mrasl.shared.IDisposable;
import ca.polymtl.mrasl.shared.IListenerCaller;

/**
 * This class contains the list of detected tags in the mission.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class TagList implements IDisposable, IListenerCaller<TagAddedListener> {

    // ---------------------------------------------------------------------------------------------
    // Attributes
    // ---------------------------------------------------------------------------------------------

    private final List<Tag> fList = new ArrayList<>();
    private final TagSurvivorSubscriberNode fSubscriber = new TagSurvivorSubscriberNode(this);
    private final List<TagAddedListener> fListeners = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------------------------------

    public TagList() {
        /* Start the subscriber node */
        RosConnection.getInstance().launchNode(fSubscriber);
    }

    // ---------------------------------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------------------------------

    /**
     * This method adds a tag to the list of detected tags.
     *
     * @param tag The new detected tag
     */
    public void addTag(Tag tag) {
        fList.add(tag);

        /* Call each listener for a new tag */
        for (TagAddedListener listener : fListeners) {
            listener.onNewTag(tag);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Overriden methods
    // ---------------------------------------------------------------------------------------------

    @Override
    public void dispose() {
        /* Shutdown the subscriber node */
        RosConnection.getInstance().shutdownNode(fSubscriber);
    }

    @Override
    public void addListener(TagAddedListener listener) {
        /* Make sure the listener isn't already connected */
        if (fListeners.contains(listener)) {
            return;
        }

        /* Add the listener to the list */
        fListeners.add(listener);
    }

    @Override
    public void removeListener(TagAddedListener listener) {
        /* Make sure the listener is connected */
        if (!fListeners.contains(listener)) {
            return;
        }

        /* Remove the listener to the list */
        fListeners.remove(listener);
    }

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * Accessor that returns an immutable list of detected tag.
     *
     * @return The list of detected tag
     */
    public List<Tag> getList() {
        return ImmutableList.copyOf(fList);
    }

}
