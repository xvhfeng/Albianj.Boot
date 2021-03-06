package org.albianj.framework.boot.servants;

import org.albianj.framework.boot.logging.LogServant;
import org.albianj.framework.boot.logging.LoggerLevel;
import org.albianj.framework.boot.tags.BundleSharingTag;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * xml文件的解析context
 * XmlParserContext将会保存xml的doc，filename与load文件时的sessionid，方便对于日志的记录
 * 所有的value都会被执行trim()操作
 */
@BundleSharingTag
public class XmlParserContext {

    private Document doc;
    private String filename;
    private String sessionId;
    private  XPath xpSeleter;

    protected XmlParserContext(String sessionId, String filename) {
        this.filename = filename;
        this.sessionId = sessionId;

    }

    public XmlParserContext() {

    }

    public static XmlParserContext makeXmlParserContext(String sessionId, String filename) {
        XmlParserContext ctx = null;
        try {
            ctx = new XmlParserContext(sessionId, filename);
        } catch (Exception e) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(XmlParserContext.class)
                    .alwaysThrow(true)
                    .withCause(e)
                    .takeBrief("XmlParser Error")
                    .addMessage("New XmlParserContext for xml file -> {0} is fail.", filename)
                    .build().toLogger();
        }
        return ctx;
    }

    /**
     * 加载xml文档
     *
     * @return
     */
    public XmlParserContext load() {
        if (StringServant.Instance.isNullOrEmptyOrAllSpace(this.filename)) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("The xml filename is null or empty.")
                    .build().toLogger();
        }
        if (FileServant.Instance.isFileOrPathNotExist(this.filename)) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("The xml filename -> {0} is not exist.", this.filename)
                    .build().toLogger();
        }
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.filename);
        } catch (Exception e) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .withCause(e)
                    .takeBrief("XmlParser Error")
                    .addMessage("Load and parser xml filename -> {0} is error.", this.filename)
                    .build().toLogger();
        }
        this.doc = doc;
        XPathFactory xpf = XPathFactory.newInstance();
        xpSeleter =  xpf.newXPath();
//        try {
//            NodeList links = (NodeList) xp.evaluate("Bundles/Bundle",doc, XPathConstants.NODESET);
//        } catch (XPathExpressionException e) {
//            e.printStackTrace();
//        }
        return this;
    }

    /**
     * 在文档中根据指定的名字选择节点集合
     *
     * @param tagName
     * @return
     */
    public List<Node> selectNodes(String tagName, boolean throwIfNotExist) {
        if (StringServant.Instance.isNullOrEmptyOrAllSpace(tagName)) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("Argument tagName -> {0} for parsering conf -> {1} is null or empty.", tagName, this.filename)
                    .build().toLogger();
        }
        NodeList nodes = null;
        try {
            nodes = (NodeList) xpSeleter.evaluate(tagName,doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

//        NodeList nodes = doc.getElementsByTagName(tagName);
        if (null == nodes || 0 == nodes.getLength()) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage("Nodes by tagName -> {0} not in XmlDoc file -> {1}.", tagName, this.filename)
                        .build().toLogger();
            }
            return null;
        }
        List<Node> list = new ArrayList<>();
        Node son = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            son = nodes.item(i);
            list.add(son);
        }
        return list;
    }

    /**
     * 在文档中根据指定的名字选择节点
     *
     * @param tagName
     * @return
     */
    public Node selectNode(String tagName, boolean throwIfNotExist) {
        List<Node> nodes = selectNodes(tagName, false);
        if (null == nodes) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage("Nodes by tagName -> {0} not in file -> {1}.", tagName, this.filename)
                        .build().toLogger();
            }
            return null;
        }
        if (2 <= nodes.size()) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("TagName -> {0} node count ->{1} in file -> {2} is not single.", tagName, nodes.size(), this.filename)
                    .build().toLogger();
        }
        return nodes.get(0);
    }

    /**
     * 从节点中根据指定的名字选定子节点集合
     *
     * @param parent
     * @param tagName
     * @return
     */
    public List<Node> selectNodes(Node parent, String tagName, boolean throwIfNotExist) {
        if (!parent.hasChildNodes()) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage("Nodes by tagName -> {0} not inside parentNode -> {1} (By the way parentNode has no one childNode.) in file -> {2}.",
                                tagName, parent.getNodeName(), this.filename)
                        .build().toLogger();
            }
            return null;
        }

        NodeList nodes = null;
        try {
            nodes = (NodeList) xpSeleter.evaluate(tagName,parent, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

//        NodeList nodes = parent.getChildNodes();
        List<Node> list = new ArrayList<>();
        Node son = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            son = nodes.item(i);
            if (son.getNodeName().equalsIgnoreCase(tagName)) {
                list.add(son);
            }
        }
        if (0 != list.size()) {
            return list;
        }
        if (throwIfNotExist) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("Nodes by tagName -> {0} not inside parentNode -> {1} in file -> {2}.",
                            tagName, parent.getNodeName(), this.filename)
                    .build().toLogger();
        }
        return null;
    }

    /**
     * 从节点中根据指定的名字选定单独子节点
     *
     * @param parent
     * @param tagName
     * @return
     */
    public Node selectNode(Node parent, String tagName, boolean throwIfNotExist) {
        List<Node> nodes = selectNodes(parent, tagName, false);
        if (null == nodes) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage("Nodes by tagName -> {0} not inside parentNode -> {1} in file -> {2}.",
                                tagName, parent.getNodeName(), this.filename)
                        .build().toLogger();

            }
            return null;
        }
        if (2 <= nodes.size()) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("TagName -> {0} node count ->{1} is not single.", tagName, nodes.size())
                    .build().toLogger();
        }
        return nodes.get(0);
    }

    /**
     * 得到xml节点的属性或者是子节点的值
     * 节点分大小写
     * 如果没有属性或者没有节点，可以设置是否抛出异常
     *
     * @param node
     * @param attrOrCNodeName
     * @param throwIfNotExist
     * @return
     */
    public String findValueByAttrOrChildNode(Node node, String attrOrCNodeName, boolean allowValueNullOrEmpty, boolean throwIfNotExist) {
        String val = findAttributeValue(node, attrOrCNodeName, allowValueNullOrEmpty, false);
        if (null == val) {
            val = findChildNodeValue(node, attrOrCNodeName, allowValueNullOrEmpty, false);
        }

        if (null == val && allowValueNullOrEmpty) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage( "Attribute or childNode -> {0} not in parent -> {1}.",
                            attrOrCNodeName, node.getNodeName())
                    .build().toLogger();
        }
        return  null == val ? null : val.trim();
    }

    /**
     * 得到xml节点的属性或者是子节点的值
     * 节点分大小写
     * 没有节点返回默认值
     *
     * @param node
     * @param attrOrCNodeName
     * @param defer
     * @return
     */
    public String findValueByAttrOrChildNode(Node node, String attrOrCNodeName, String defer) {
        String val = findValueByAttrOrChildNode(node, attrOrCNodeName, true, false);
        return StringServant.Instance.isNullOrEmptyOrAllSpace(val) ? defer : val;
    }

    public String findAttributeValue(Node node, String attrName, boolean allowValueNullOrEmpty, boolean throwIfNotExist) {
        if (!node.hasAttributes()) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage( "Node -> {0} has not any attribute in file -> {1}。So attribute -> {2} is not exist.",
                                node.getNodeName(), this.filename, attrName)
                        .build().toLogger();
            }
            return null;
        }

        Node son = null;
        NamedNodeMap nodeMap = node.getAttributes();
        son = nodeMap.getNamedItem(attrName);
        if (null == son) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage("Attribute -> {0} not in Node -> {1} in file -> {3}.",
                                attrName, node.getNodeName(), this.filename)
                        .build().toLogger();
            }
            return null;
        }
        String val = son.getTextContent();
        if (StringServant.Instance.isNullOrEmptyOrAllSpace(val) && !allowValueNullOrEmpty) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("Attribute -> {0} value is null or empry in Node -> {1} in file -> {3}.",
                            attrName, node.getNodeName(), this.filename)
                    .build().toLogger();

        }
        return  null == val ? null : val.trim();
    }

    public String findAttributeValue(Node node, String attrName, String defer) {
        String val = findAttributeValue(node, attrName, true, false);
        return StringServant.Instance.isNullOrEmptyOrAllSpace(val) ? defer : val;
    }

    public String findChildNodeValue(Node parent, String childNodeName, boolean allowValueNullOrEmpty, boolean throwIfNotExist) {
        if (!parent.hasChildNodes()) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage("Parent node -> {0} has no one child node in the file -> {1}.",
                                parent.getNodeName(), this.filename)
                        .build().toLogger();
            }
            return null;
        }

        Node node = selectNode(parent, childNodeName, false);
        if (null == node) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage("Parent node -> {0} has no one child node named -> {1} in the file -> {2}.",
                                parent.getNodeName(), childNodeName, this.filename)
                        .build().toLogger();
            }
            return null;
        }

        String val = node.getTextContent();
        if (StringServant.Instance.isNullOrEmptyOrAllSpace(val) && !allowValueNullOrEmpty) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("Node -> {0} inside in parent node -> {1} in the file -> {2} that value is null or empty,but not allow.",
                            childNodeName, parent.getNodeName(), this.filename)
                    .build().toLogger();
        }
        return  null == val ? null : val.trim();

    }

    public String findChildNodeValue(Node parent, String childNodeName, String defer) {
        String val = findChildNodeValue(parent, childNodeName, true, false);
        return StringServant.Instance.isNullOrEmptyOrAllSpace(val) ? defer : val;
    }

    public String findNodeValue(Node node, boolean allowValueNullOrEmpty) {
        String val = node.getTextContent();
        if (StringServant.Instance.isNullOrEmptyOrAllSpace(val) && !allowValueNullOrEmpty) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage("Node -> {0}  in the file -> {2} that value is null or empty,but not allow.",
                            node.getNodeName(), this.filename)
                    .build().toLogger();
        }
        return null == val ? null : val.trim();
    }

    public String findNodeValue(Node node, String defer) {
        String val = node.getTextContent();
        return StringServant.Instance.isNullOrEmptyOrAllSpace(val) ? defer : val;
    }

    public String findNodeValue(String nodeName, boolean allowValueNullOrEmpty, boolean throwIfNotExist) {
        Node node = selectNode(nodeName, false);
        if (null == node) {
            if (throwIfNotExist) {
                LogServant.Instance.newLogPacketBuilder()
                        .forSessionId(sessionId)
                        .atLevel(LoggerLevel.Error)
                        .byCalled(this.getClass())
                        .alwaysThrow(true)
                        .takeBrief("XmlParser Error")
                        .addMessage( sessionId, LoggerLevel.Error,
                                this.getClass(), null, "XmlParser Error.", null,
                                "Nodes by tagName -> {0} not in file -> {1}.",
                                nodeName, this.filename)
                        .build().toLogger();
            }
            return null;
        }
        String val = node.getTextContent();
        if (StringServant.Instance.isNullOrEmptyOrAllSpace(val) && !allowValueNullOrEmpty) {
            LogServant.Instance.newLogPacketBuilder()
                    .forSessionId(sessionId)
                    .atLevel(LoggerLevel.Error)
                    .byCalled(this.getClass())
                    .alwaysThrow(true)
                    .takeBrief("XmlParser Error")
                    .addMessage( "Node -> {0}  in the file -> {2} that value is null or empty,but not allow.",
                            node.getNodeName(), this.filename)
                    .build().toLogger();
        }
        return null == val ? null : val.trim();
    }

    public String findNodeValue(String nodeName, String defer) {
        String val = findNodeValue(nodeName, true, false);
        return StringServant.Instance.isNullOrEmptyOrAllSpace(val) ? defer : val;
    }
}
