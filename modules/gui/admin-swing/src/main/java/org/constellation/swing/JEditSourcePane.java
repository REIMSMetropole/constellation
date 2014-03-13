/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.swing;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.xml.namespace.QName;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.Layer;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.util.DataReference;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.openide.util.NbBundle;

/**
 *
 * @author guilhem
 */
public class JEditSourcePane extends javax.swing.JPanel {

    private SourceModel sourceModel;

    /**
     * Creates new form JEditSourcePane
     * @param server
     * @param serviceType
     * @param sourceModel
     */
    public JEditSourcePane(final ConstellationServer server, final String serviceType, final SourceModel sourceModel) {
        this.sourceModel = sourceModel;
        initComponents();

        //create combobox items (dataReference string)
        final List<String> providerList = new ArrayList<>();

        final ProvidersReport providersReport = server.providers.listProviders();
        final List<ProviderServiceReport> servicesReport = providersReport.getProviderServices();
        for(final ProviderServiceReport serviceReport : servicesReport) {
            final String serviceProviderType = serviceReport.getType();

            final List<ProviderReport> providers = serviceReport.getProviders();
            for (final ProviderReport providerReport : providers) {

                boolean addProviderToList = false;
                //WFS -> data-store
                if ( ("WFS".equals(serviceType) && "feature-store".equals(serviceProviderType)) ) {
                    addProviderToList = true;
                }

                //WMTS or WCS -> coverage-store
                if ( ("WMTS".equals(serviceType) || "WCS".equals(serviceType) ) && "coverage-store".equals(serviceProviderType)) {
                    addProviderToList = true;
                }

                //WMS -> all layer provider
                if ("WMS".equals(serviceType)) {
                    addProviderToList = true;
                }

                if (addProviderToList) {
                    providerList.add(providerReport.getId());
                }
            }
        }

        guiSourceList.setModel(new ListComboBoxModel(providerList));
        

        if (sourceModel != null) {
            guiSourceList.setSelectedItem(sourceModel.getProviderId());
            guiLoadAllBox.setSelected(sourceModel.isLoadAll());
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        guiSourceList = new JComboBox();
        jLabel2 = new JLabel();
        guiLoadAllBox = new JCheckBox();

        jLabel1.setText("Source");

        jLabel2.setText(NbBundle.getMessage(JProviderEditPane.class, "loadAll")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(guiLoadAllBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(guiSourceList, 0, 369, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel1, jLabel2});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(guiSourceList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(guiLoadAllBox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiSourceList, jLabel1, jLabel2});

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox guiLoadAllBox;
    private JComboBox guiSourceList;
    private JLabel jLabel1;
    private JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    public SourceModel getSourceEntry() {
        if (sourceModel == null) {
            sourceModel = new SourceModel();
        }

        sourceModel.setProviderId((String)guiSourceList.getSelectedItem());
        sourceModel.setLoadAll(guiLoadAllBox.isSelected());
        
        return sourceModel;
    }


    public static SourceModel showDialog(final ConstellationServer server, final String serviceType, final SourceModel source) {

        final JEditSourcePane pane = new JEditSourcePane(server, serviceType, source);

        int res = JOptionPane.showOptionDialog(null, new Object[]{pane},
                LayerRowModel.BUNDLE.getString("createSourceMsg"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                null);

        if (res == JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION) {
            return null;
        }
        return pane.getSourceEntry();
    }
}
