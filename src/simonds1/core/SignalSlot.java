/* 
    This piece of simple code is provided for free.
    I would really love that you atleast include the comments in you code base, 
    even if you may remove my name.
 */
package simonds1.core;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A pretty simple signal and slot event polling system. Its not meant to
 * replace java's event handling, only meant to improve it by giving developers
 * the ability to handle events from any unknown source within an application.
 * <hr />
 *
 * @author OLAGOKE FAROUQ <olagokedammy@gmail.com>
 */
public class SignalSlot {

    /**
     * On emitting a signal, we check through slots and execute slots with no or
     * matching predicates only
     *
     * @param slotGroupID This string is used to id the signal emitted
     * @param slotID
     * @param message The message the signal wants to pass to a slot is boxed up
     * here
     * @return the boolean
     */
    public static boolean emitSignal(String slotGroupID, String slotID, Object message) {
        if (!hasSlot(slotGroupID, slotID)) {
            return false;
        }
        //System.out.println("Has Slot " + slotID);
        try {
            SLOT_MAP.get(slotGroupID).get(slotID).stream().forEach(slot -> {
                if (slot.getPredicate() == null) //both if conditions would be unified in kotlin
                {
                    slot.exec(message);
                } else if (slot.getPredicate().equals(message)) {
                    slot.exec(message);
                }
            });
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Clears the slots registered with <i><b>slotId</b></i>
     *
     * @param slotGroupID The ID token used to identify a group of slots
     * associated with a map.
     * @param slotID the slot ID
     * @return the boolean
     */
    public static boolean removeSlots(String slotGroupID, String slotID) {
        if (!hasSlot(slotGroupID, slotID)) {
            return false;
        }
        SLOT_MAP.get(slotGroupID).remove(slotID);
        return true;
    }

    public static boolean removeSlotGroup(String slotGroupID) {
        if (!hasSlotGroup(slotGroupID)) {
            return false;
        }
        SLOT_MAP.remove(slotGroupID);
        return true;
    }

    private static boolean hasSlotGroup(String slotGroupID) {
        return (slotGroupID == null) ? false : SLOT_MAP.containsKey(slotGroupID);
    }

    private static boolean hasSlot(String slotGroupID, String slotID) {
        //all operations are localized to each slot group and not globally
        return (hasSlotGroup(slotGroupID) && slotID != null) ? SLOT_MAP.get(slotGroupID).containsKey(slotID) : false;

    }

    /**
     * Adds a slot to the slot poll. The slot will only be triggered by a
     * matching signal ID.
     *
     * @param slotGroupID
     * @param slotID The ID of the signal that will trigger this slot
     * @param slot The slot object that represents the slot's intention
     * @return the boolean
     */
    public static boolean addSlot(String slotGroupID, String slotID, CSlot slot) {
        if (slotGroupID == null) {
            return false;           //every slotID must fall under a slotGroupID
        }
        try {
            if (!SLOT_MAP.containsKey(slotGroupID)) {
                HashMap<String, ArrayList<CSlot>> tmpMap = new HashMap<>();
                ArrayList<CSlot> tmp = new ArrayList<>();
                tmp.add(slot);
                tmpMap.put(slotID, tmp);
                SLOT_MAP.put(slotGroupID, tmpMap);
            } else {
                if (!SLOT_MAP.get(slotGroupID).containsKey(slotID)) {
                    ArrayList<CSlot> tmp = new ArrayList<>();
                    tmp.add(slot);
                    SLOT_MAP.get(slotGroupID).put(slotID, tmp);
                }
                if (!SLOT_MAP.get(slotGroupID).get(slotID).contains(slot)) { //so we are sure slotGroupID exists here
                    SLOT_MAP.get(slotGroupID).get(slotID).add(slot);
                }
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }
    private static final HashMap<String, HashMap<String, ArrayList<CSlot>>> SLOT_MAP = new HashMap<>();
    //HashMap<signalGroupID, HashMap<signalID,ArrayList<CSlot>>>
}
