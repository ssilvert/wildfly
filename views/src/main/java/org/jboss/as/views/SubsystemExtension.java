package org.jboss.as.views;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import java.util.Collections;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

import org.jboss.as.controller.PathAddress;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;


/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class SubsystemExtension implements Extension {

    /**
     * The name space used for the {@code substystem} element
     */
    public static final String NAMESPACE = "urn:jboss:domain:views:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = SubsystemDefinition.SUBSYS_NAME;

    public static final int SUBSYSTEM_MAJOR_VERSION = 1;
    public static final int SUBSYSTEM_MINOR_VERSION = 0;

    /**
     * The parser used for parsing our subsystem
     */
    private final SubsystemParser parser = new SubsystemParser();

    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    private static final String RESOURCE_NAME = SubsystemExtension.class.getPackage().getName() + ".LocalDescriptions";

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, SubsystemExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }


    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, SUBSYSTEM_MAJOR_VERSION, SUBSYSTEM_MINOR_VERSION);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SubsystemDefinition.INSTANCE);
        registration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE, GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        registration.registerSubModel(new ViewDefinition());
        subsystem.registerXMLElementWriter(parser);
    }

    private static ModelNode createAddSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
            context.startSubsystemElement(SubsystemExtension.NAMESPACE, false);
            System.out.println("context.getModelNode()");
            System.out.println(context.getModelNode().toString());
            for (Property view : context.getModelNode().get("view").asPropertyList()) {
                writer.writeStartElement("view");
                writer.writeAttribute("name", view.getName());
                writer.writeAttribute("definition", view.getValue().get("definition").asString());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            // Require no attributes
            ParseUtils.requireNoAttributes(reader);

            list.add(createAddSubsystemOperation());

            while(reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (!reader.getLocalName().equals("view")) {
                    throw ParseUtils.unexpectedElement(reader);
                }

                readView(reader, list);

                while(reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                    // move to next <view> element
                    // TODO: find a more proper way to do this
                }
            }
        }

        private void readView(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            String name = null;
            String definition = null;
            for (int i=0; i < reader.getAttributeCount(); i++) {
                String attr = reader.getAttributeLocalName(i);
                if (attr.equals("name")) {
                    name = reader.getAttributeValue(i);
                    continue;
                }

                if (attr.equals("definition")) {
                    definition = reader.getAttributeValue(i);
                    continue;
                }

                throw ParseUtils.unexpectedAttribute(reader, i);
            }

            if (name == null) throw ParseUtils.missingRequired(reader, Collections.singleton("name"));
            if (definition == null) throw ParseUtils.missingRequired(reader, Collections.singleton("definition"));

            ModelNode addView = new ModelNode();
            addView.get(OP).set(ModelDescriptionConstants.ADD);
            PathAddress addr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME),
                                                       PathElement.pathElement("view", name));
            addView.get(OP_ADDR).set(addr.toModelNode());

            ModelNode descriptionNode = new ModelNode();
            descriptionNode.get("definition").set(definition);

            addView.get("definition").set(definition);

            list.add(addView);
        }
    }

}
