/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.swing;

import java.awt.BorderLayout;
import java.util.logging.Level;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.configuration.AbstractConfigurationObject;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JServiceSosEditPane extends JServiceEditionPane {

    private ConstellationClient serverV2;
    private Instance serviceInstance;
    private SOSConfiguration configuration;
    private JServiceEditionPane OMspecificPane;
    private JServiceEditionPane SMLspecificPane;
    
    
    public JServiceSosEditPane(final ConstellationClient server, final Instance serviceInstance, final Object configuration) {
        this.serverV2 = serverV2;
        this.serviceInstance = serviceInstance;
        this.configuration = (configuration instanceof SOSConfiguration) ? (SOSConfiguration) configuration : null;
        initComponents();
        if (this.configuration != null) {
            
            if (this.configuration.getSMLConfiguration() != null) {
                if ("mdweb".equals(this.configuration.getSMLConfiguration().getFormat())) {
                    SMLspecificPane = new JCswMdwEditPane(this.configuration.getSMLConfiguration());
                    SMLspecificPane.setSize(562, 298);
                    SMLPane.add(BorderLayout.CENTER, SMLspecificPane);
                } else if ("filesystem".equals(this.configuration.getSMLConfiguration().getFormat())) {
                    SMLspecificPane = new JCswFsEditPane(this.configuration.getSMLConfiguration());
                    SMLspecificPane.setSize(450, 86);
                    SMLPane.add(BorderLayout.CENTER, SMLspecificPane);
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected SML format:{0}", this.configuration.getSMLConfiguration().getFormat());
                }
                guiOMDataSourceCombo.setSelectedItem(this.configuration.getSMLConfiguration().getFormat());
            }
            
            if (this.configuration.getOMConfiguration() != null) {
                if ("OM2".equals(this.configuration.getOMConfiguration().getFormat())) {
                    OMspecificPane = new JSosOM2EditPane(this.configuration.getOMConfiguration());
                    OMspecificPane.setSize(562, 298);
                    OMPane.add(BorderLayout.CENTER, OMspecificPane);
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected OM format:{0}", this.configuration.getOMConfiguration().getFormat());
                }
                guiOMDataSourceCombo.setSelectedItem(this.configuration.getOMConfiguration().getFormat());
            }
            
            
            if (this.configuration.getLogLevel() != null) {
                this.logLevelCombo.setSelectedItem(this.configuration.getLogLevel().getName());
            }
        }
        repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        logLevelCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        guiSMLDataSourceCombo = new javax.swing.JComboBox();
        SMLPane = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        guiOMDataSourceCombo = new javax.swing.JComboBox();
        OMPane = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/constellation/swing/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("logLevel")); // NOI18N

        logLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "INFO", "FINE", "FINER" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("smlSourceType")); // NOI18N

        guiSMLDataSourceCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "mdweb", "filesystem" }));
        guiSMLDataSourceCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                guiSMLDataSourceComboItemStateChanged(evt);
            }
        });

        SMLPane.setPreferredSize(new java.awt.Dimension(236, 256));

        javax.swing.GroupLayout SMLPaneLayout = new javax.swing.GroupLayout(SMLPane);
        SMLPane.setLayout(SMLPaneLayout);
        SMLPaneLayout.setHorizontalGroup(
            SMLPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        SMLPaneLayout.setVerticalGroup(
            SMLPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 249, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("omSourceType")); // NOI18N

        guiOMDataSourceCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OM2" }));
        guiOMDataSourceCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                guiOMDataSourceComboItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout OMPaneLayout = new javax.swing.GroupLayout(OMPane);
        OMPane.setLayout(OMPaneLayout);
        OMPaneLayout.setHorizontalGroup(
            OMPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        OMPaneLayout.setVerticalGroup(
            OMPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 276, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(OMPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiOMDataSourceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(SMLPane, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logLevelCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guiSMLDataSourceCombo, 0, 479, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(guiSMLDataSourceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(SMLPane, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(guiOMDataSourceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(OMPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(logLevelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void guiSMLDataSourceComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_guiSMLDataSourceComboItemStateChanged
        SMLPane.removeAll();
        if ("mdweb".equals(guiSMLDataSourceCombo.getSelectedItem())) {
            SMLspecificPane = new JCswMdwEditPane(this.configuration.getOMConfiguration());
            SMLspecificPane.setSize(562, 278);
            SMLPane.add(BorderLayout.CENTER, SMLspecificPane);
        } else if ("filesystem".equals(guiSMLDataSourceCombo.getSelectedItem())) {
            SMLspecificPane = new JCswFsEditPane(this.configuration.getOMConfiguration());
            SMLspecificPane.setSize(450, 86);
            SMLPane.add(BorderLayout.CENTER, SMLspecificPane);
        } else {
            LOGGER.log(Level.WARNING, "Unexpected SML format:{0}", guiSMLDataSourceCombo.getSelectedItem());
        }
        validate();
        repaint();
    }//GEN-LAST:event_guiSMLDataSourceComboItemStateChanged

    private void guiOMDataSourceComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_guiOMDataSourceComboItemStateChanged
        OMPane.removeAll();
        if ("OM2".equals(guiOMDataSourceCombo.getSelectedItem())) {
            OMspecificPane = new JSosOM2EditPane(this.configuration.getOMConfiguration());
            OMspecificPane.setSize(562, 278);
            OMPane.add(BorderLayout.CENTER, OMspecificPane);
        }else {
            LOGGER.log(Level.WARNING, "Unexpected OM format:{0}", guiOMDataSourceCombo.getSelectedItem());
        }
        validate();
        repaint();
    }//GEN-LAST:event_guiOMDataSourceComboItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OMPane;
    private javax.swing.JPanel SMLPane;
    private javax.swing.JComboBox guiOMDataSourceCombo;
    private javax.swing.JComboBox guiSMLDataSourceCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox logLevelCombo;
    // End of variables declaration//GEN-END:variables

    private void updateConfiguration() {
        Automatic OMConfig = null;
        if (OMspecificPane != null) {
            OMConfig = (Automatic) OMspecificPane.getConfiguration();
            
        }
        
        Automatic SMLConfig = null;
        if (OMspecificPane != null) {
            SMLConfig = (Automatic) SMLspecificPane.getConfiguration();
            
        }
        this.configuration = new SOSConfiguration(SMLConfig, OMConfig);
        
        this.configuration.setSMLType(SMLspecificPane.getDatasourceType());
        this.configuration.setObservationReaderType(OMspecificPane.getDatasourceType());
        this.configuration.setObservationFilterType(OMspecificPane.getDatasourceType());
        this.configuration.setObservationWriterType(OMspecificPane.getDatasourceType());
        
        this.configuration.setLogLevel((String)logLevelCombo.getSelectedItem());
    }
    
    @Override
    public AbstractConfigurationObject getConfiguration() {
        updateConfiguration();
        return configuration;
    }
    
    @Override
    public DataSourceType getDatasourceType() {
        throw new UnsupportedOperationException("Not supported on this panel.");
    }
}
