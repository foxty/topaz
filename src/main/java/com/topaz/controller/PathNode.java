package com.topaz.controller;

import java.util.concurrent.ConcurrentHashMap;

class PathNode {

	private int pos;
	private String nodeName;
	private PathNode parent;

	PathNode(String nodeName, PathNode parent) {
		this.pos = (parent != null ? parent.getPos() + 1 : 0);
		this.parent = parent;
		this.nodeName = nodeName;
	}

	int getPos() {
		return pos;
	}

	String getNodeName() {
		return nodeName;
	}

	PathNode getParent() {
		return parent;
	}

	String fullPath() {
		StringBuffer sb = new StringBuffer(nodeName);
		PathNode p = getParent();
		while (p != null) {
			sb.insert(0, p.getNodeName() + "/");
			p = p.getParent();
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "[PathNode: nodeName=" + nodeName + ", pos=" + pos + "]";
	}
}

class ModuleNode extends PathNode {

	private ConcurrentHashMap<String, ControllerNode> controllers = new ConcurrentHashMap<String, ControllerNode>();
	private ConcurrentHashMap<String, ModuleNode> nodes = new ConcurrentHashMap<String, ModuleNode>();

	ModuleNode(String nodeName, PathNode parent) {
		super(nodeName, parent);
	}

	void addControllerNode(ControllerNode cn) {
		controllers.putIfAbsent(cn.getNodeName(), cn);
	}

	void addModuleNode(ModuleNode node) {
		nodes.putIfAbsent(node.getNodeName(), node);
	}

	boolean hasController(String cName) {
		return controllers.containsKey(cName);
	}

	ControllerNode findControllerNode(String cName) {
		return controllers.get(cName);
	}

	boolean hasNode(String nName) {
		return nodes.containsKey(nName);
	}

	ModuleNode findNode(String nName) {
		return nodes.get(nName);
	}
}

class ControllerNode extends PathNode {
	private Controller controller;

	ControllerNode(String nodeName, Controller c, PathNode parent) {
		super(nodeName, parent);
		this.controller = c;
	}

	Controller getController() {
		return controller;
	}
}
