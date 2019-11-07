package simonds1.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import simonds1_client.modules.ElementProfile;
import simonds1_client.modules.ModuleEngine;
import simonds1_client.ui.pad.Canvas2D;
import simonds1_client.ui.shapes.CElement;
import simonds1_client.ui.shapes.CNode;

/**
 * Provides necessary functions for the command system
 *
 * @author Olagoke Adedamola Farouq
 */
//createNode(String title, String boundary, double posX, double posY, double nposX, double nposY, boolean isResultMode)
public class Commander {

    public Commander() {
        //
    }

    public String execute(String command, Canvas2D canvas) {
        command = command.toLowerCase().trim();
        if (command.contains(";")) {
            for (String cmd : command.split(";")) {
                System.out.println(cmd);
                execute(cmd.trim(), canvas);
            }
            return "Batch Command Completed.";
        }
        String[] tokens = command.split(" ");
        if (tokens.length < 2) {
            return "Unrecognized Command Sent";
        }
        //add help commands
        switch (tokens[0].trim()) {
            case "drawnode":
                if (tokens.length < 3) {
                    return "Error: Possible incomplete DRAWNODE tokens!\n Format: DRAWNODE POSX POSY\n";
                }
                return drawNode(tokens, canvas);
            case "duplnode":
                if (tokens.length < 4) {
                    return "Error: Possible incomplete DUPLNODE tokens!\n "
                            + "Format: DUPLNODE NODE-ID DIRECTION(X|Y) DISTANCE ANGLE\n";
                }
                return duplicateNode(tokens, canvas);
            case "delnode":
                if (tokens.length < 2) {
                    return "Error: Possible incomplete DELNODE tokens!\n "
                            + "Format: DELNODE NODE-ID1,NODE-ID2,NODE-ID3,...\n";
                }
                return deleteNode(tokens, canvas);
            case "delelement":
                if (tokens.length < 2) {
                    return "Error: Possible incomplete DELELEMENT tokens!\n "
                            + "Format: DELELEMENT ELEM-ID1,ELEM-ID2,ELEM-ID3,...\n";
                }
                return deleteElement(tokens, canvas);
            case "drawelement":
                if (tokens.length < 3) {
                    return "Error: Possible incomplete DRAWELEMENT tokens!\n "
                            + "Format: DRAWELEMENT NODE-ID1 NODE-ID2 NODE-ID3\n";
                }
                return drawElement(tokens, canvas);
            case "alterelement":
                if (tokens.length < 4) {
                    return "Error: Possible incomplete DELNODE tokens!\n "
                            + "Format: ALTERELEMENT ATTR VALUE ELEM-ID1,ELEM-ID2,ELEM-ID3,...\n";
                }
                return alterElement(tokens, canvas);
            case "alternode":
                if (tokens.length < 4) {
                    return "Error: Possible incomplete ALTERNODE tokens!\n "
                            + "Format: ALTERNODE ATTR VALUE NODE-ID1,NODE-ID2,NODE-ID3,...\n";
                }
                return alterNode(tokens, canvas);
            case "setprofile":
                if (tokens.length < 3) {
                    return "Error: Possible incomplete SETPROFILE tokens!\n "
                            + "Format: SETPROFILE PROFILE_NAME ELEM-ID1,ELEMID2,...\n";
                }
                return setProfile(tokens, canvas);
            //break;
        }

        return "Error: Unrecognized Command String";
    }

    private String drawNode(String[] tokens, Canvas2D canvas) {
        String[] options = new String[]{"command", "posx", "posy", "boundary", "isresult"};
        HashMap<String, String> nodeConfig = new HashMap<>();
        int i = 0;
        for (String tok : tokens) {
            if (options[i].equals("boundary")) {
                switch (tok) {
                    case "none":
                        tok = "0";
                        break;
                    case "fixed":
                        tok = "3";
                        break;
                    case "pinned":
                        tok = "2";
                        break;
                    default:
                        tok = "1";
                        break;
                }
            }
            nodeConfig.put(options[i], tok);
            i++;
        }
//move this whole system to the signal slots mechanism please?
        CNode tmpNode;
        try {
            tmpNode = canvas.createNode(null, nodeConfig.getOrDefault("boundary", "0"),
                    canvas.getScaleX(Double.valueOf(nodeConfig.getOrDefault("posx", "0")), false) + Canvas2D.CANVAS_PADDING_X,
                    canvas.getScaleY(Double.valueOf(nodeConfig.getOrDefault("posy", "0")), false) + Canvas2D.CANVAS_PADDING_Y,
                    Double.valueOf(nodeConfig.getOrDefault("posx", "0")),
                    Double.valueOf(nodeConfig.getOrDefault("posy", "0")),
                    nodeConfig.getOrDefault("isresult", "false").equals("true"));
        } catch (Exception ex) {
            tmpNode = null;
        }
        return tmpNode == null ? "Error: Failed to create Node" : "Created " + tmpNode.getTitle();
    }

    private String duplicateNode(String[] tokens, Canvas2D canvas) {
        String[] options = new String[]{"command", "nodeid", "direction", "distance", "amount", "angle"};
        HashMap<String, String> nodeConfig = new HashMap<>();
        int i = 0;
        CNode hostNode = null;
        for (String tok : tokens) {
            switch (options[i]) {
                case "nodeid":
                    hostNode = (CNode) canvas.getShape("node", tok.replace("n", "Node"));
                    if (hostNode == null) {
                        return "Error: The Host Node [" + tok + "] does not exist!\n";
                    }
                    break;
                case "direction":
                    if (!tok.equals("x") && !tok.equals("y")) {
                        return "Error: Expected Direction token is either 'X' or 'Y'\n";
                    }
                    break;
                case "angle":
                    if (Float.parseFloat(tok) < 0) {
                        tok = "0";
                    }
                    break;
            }
            nodeConfig.put(options[i], tok);
            i++;
        }
        double asp = Float.parseFloat(nodeConfig.get("distance")),
                sp = canvas.getScaleX(Float.parseFloat(nodeConfig.get("distance")), false),
                nsp = asp;
        double x, y, nx, ny;
        double angle = Float.parseFloat(nodeConfig.getOrDefault("angle", "0"));
        int amount = Integer.parseInt(nodeConfig.getOrDefault("amount", "1"));
        if (nodeConfig.get("direction").equals("x")) {
            x = hostNode.getCenterX() + sp;
            y = hostNode.getCenterY();
            nx = hostNode.nx.doubleValue() + nsp;
            ny = hostNode.ny.doubleValue();
            while (amount-- > 0) {
                canvas.createNode(null, "0",
                        angle == 0 ? x : x + Math.cos(Math.toRadians(angle)) * asp,
                        angle == 0 ? y : y + Math.sin(Math.toRadians(angle)) * asp,
                        nx, ny, false).setLoad(hostNode.hLoad.get(), hostNode.vLoad.get());
                asp += sp;
                x += sp;
                nx += nsp;
            }
        } else {
            x = hostNode.getCenterX();
            y = hostNode.getCenterY() - sp;
            nx = hostNode.nx.doubleValue();
            ny = hostNode.ny.doubleValue() + nsp;
            while (amount-- > 0) {
                canvas.createNode(null, "0",
                        angle == 0 ? x : x + Math.cos(Math.toRadians(angle)) * asp,
                        angle == 0 ? y : y + Math.sin(Math.toRadians(angle)) * asp,
                        nx, ny, false).setLoad(hostNode.hLoad.get(), hostNode.vLoad.get());
                asp += sp;
                y += sp;
                ny += nsp;
            }
        }

        return "Node Duplicated";
    }

    private String alterNode(String[] tokens, Canvas2D canvas) {
        CNode tmpNode = null;
        String resp = "",
                attr = tokens[1],
                value = tokens[2];
        String[] elemTokens = (SimonUtil.ArrayToString(tokens, 3, null) + ",").split(","); //cmd:0 attr:1 val:2 elems:3
        //System.out.println(Arrays.toString(elemTokens));
        for (String elemid : elemTokens) {
            if (elemid.isEmpty()) {
                continue;
            }
            tmpNode = (CNode) canvas.getShape("node", elemid.replace("n", "Node"));
            if (tmpNode == null) {
                resp += "Error: The Node [" + elemid + "] does not exist!\n";
                continue;
            }
            //please contain the errors that may come due to bad values
            switch (attr) {
                case "boundary":
                    tmpNode.boundary.setValue(SimonUtil.toCamelCase(value));
                    break;
                case "h_loading":
                    tmpNode.hLoad.setValue(Double.valueOf(value));
                    break;
                case "v_loading":
                    tmpNode.vLoad.setValue(Double.valueOf(value));
                    break;
                default:
                    return "Error: No Attribute [" + attr + "] was found on " + tmpNode.getTitle() + "\n";
            }
            resp += "Altered " + attr + " with value " + value + " on " + tmpNode.getTitle() + "\n";
        }
        return resp;
    }

    private String deleteNode(String[] tokens, Canvas2D canvas) {
        //add ability to clear all nodes
        CNode tmpNode = null;
        String resp = "";
        tokens = (SimonUtil.ArrayToString(tokens, 1, null) + ",").split(",");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            tmpNode = (CNode) canvas.getShape("node", token.replace("n", "Node"));
            if (tmpNode == null) {
                resp += "Error: The Node [" + token + "] does not exist!\n";
                continue;
            }
            tmpNode.destroy();
            resp += "Deleted " + tmpNode.getTitle() + "\n";
        }
        return resp;
    }

    private String drawElement(String[] tokens, Canvas2D canvas) {
        CNode tmpNode[] = new CNode[3];
        CElement tmpElem = null;
        String tok;
        for (int i = 1; i < tokens.length; i++) {
            tok = tokens[i];
            if (tok.isEmpty()) {
                continue;
            }
            tmpNode[i - 1] = (CNode) canvas.getShape("node", tok.replace("n", "Node"));
            if (tmpNode[i - 1] == null) {
                return "Error: The Node [" + tok + "] does not exist!\n";
            }
            //just to check the correctness of the node objects! thats what the for loop is for
        }
        if (tokens.length == 3) {
            tmpElem = canvas.createElements(null, tmpNode[0], tmpNode[1], null, false);
        } else if (tokens.length > 3) {
            tmpElem = canvas.createElements(null, tmpNode[0], tmpNode[1], tmpNode[2], false);
        }

        return (tmpElem == null) ? "Error: Failed to Create Element" : tmpElem.getTitle() + " created";
    }

    private String alterElement(String[] tokens, Canvas2D canvas) {
        CElement tmpNode = null;
        String resp = "",
                attr = tokens[1],
                value = tokens[2];
        String[] elemTokens = (SimonUtil.ArrayToString(tokens, 3, null) + ",").split(","); //cmd:0 attr:1 val:2 elems:3
        for (String elemid : elemTokens) {
            if (elemid.isEmpty()) {
                continue;
            }
            tmpNode = (CElement) canvas.getShape("line", elemid.replace("e", "Element"));
            if (tmpNode == null) {
                resp += "Error: The Element [" + elemid + "] does not exist!\n";
                continue;
            }
            //please contain the errors that may come due to bad values
            switch (attr) {
                case "load_type":
                    tmpNode.loadType.setValue(value);
                    break;
                case "load_value":
                    tmpNode.loadValue.setValue(value);
                    break;
                case "load_d2o":
                    tmpNode.loadD2O.setValue(Double.valueOf(value));
                    break;
                case "elem_area":
                    tmpNode.carea = value;
                    break;
                case "elem_inertia":
                    tmpNode.inertia = value;
                    break;
                case "elem_ym":
                    tmpNode.ym = value;
                    break;
                default:
                    return "Error: No Attribute [" + attr + "] was found on " + tmpNode.getTitle() + "\n";
            }
            resp += "Altered " + attr + " with value " + value + " on " + tmpNode.getTitle() + "\n";
        }
        return resp;
    }

    private String deleteElement(String[] tokens, Canvas2D canvas) {
        //add ability to clear all elements
        CElement tmpNode = null;
        String resp = "";
        tokens = (tokens[1] + ",").split(",");
        tokens = (SimonUtil.ArrayToString(tokens, 1, null) + ",").split(",");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            tmpNode = (CElement) canvas.getShape("line", token.replace("e", "Element"));
            if (tmpNode == null) {
                resp += "Error: The Element [" + token + "] does not exist!\n";
                continue;
            }
            tmpNode.destroy();
            resp += "Deleted " + tmpNode.getTitle() + "\n";
        }
        return resp;
    }

    private String setProfile(String[] tokens, Canvas2D canvas) {
        //add ability to clear all elements
        CElement tmpNode = null;
        String resp = "";
        String[] ntokens = (SimonUtil.ArrayToString(tokens, 2, null) + ",").split(",");
        int containsFlag = 0; //if is 1, then contains in profiles list
        for (String token : ntokens) {
            if (token.isEmpty()) {
                continue;
            }
            tmpNode = (CElement) canvas.getShape("line", token.replace("e", "Element"));
            if (tmpNode == null) {
                resp += "Error: The Element [" + token + "] does not exist!\n";
                continue;
            }
            for (ElementProfile profile : ModuleEngine.ELEM_PROFILES) {
                if (profile.toString().equalsIgnoreCase(tokens[1])) {
                    containsFlag = 1; //found it
                    tmpNode.carea = String.valueOf(profile.crsArea);
                    tmpNode.ym = String.valueOf(profile.youngModulus);
                    tmpNode.inertia = String.valueOf(profile.mmi);
                    resp += tmpNode.getTitle() + " set to " + profile.name + " profile\n";
                }
            }
            if (containsFlag == 0) {
                resp = "Error: No Element Profile was set; invalid profile name provided.";
            }
        }
        return resp;
    }
}
