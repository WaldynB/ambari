/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var App = require('app');

App.MainMirroringManageClustersController = Em.ArrayController.extend({
  name: 'mainMirroringManageClustersController',

  // link to popup object
  popup: null,

  clusters: [],

  newCluster: null,

  isLoaded: function () {
    return App.router.get('mainMirroringController.isLoaded');
  }.property('App.router.mainMirroringController.isLoaded'),

  onLoad: function () {
    if (this.get('isLoaded')) {
      var clusters = [];
      App.TargetCluster.find().forEach(function (cluster) {
        var newCluster = {
          name: cluster.get('name'),
          execute: cluster.get('execute'),
          workflow: cluster.get('workflow'),
          write: cluster.get('write'),
          readonly: cluster.get('readonly'),
          staging: cluster.get('staging'),
          working: cluster.get('working'),
          temp: cluster.get('temp')
        };
        // Source cluster should be shown on top
        if (cluster.get('name') === App.get('clusterName')) {
          clusters.unshift(Ember.Object.create(newCluster));
        } else {
          clusters.push(Ember.Object.create(newCluster));
        }
      }, this);
      this.set('clusters', clusters);
    }
  }.observes('isLoaded'),

  selectedCluster: null,

  // Disable input fields for already created clusters
  isEditDisabled: function () {
    return !this.get('clustersToCreate').mapProperty('name').contains(this.get('selectedCluster.name'));
  }.property('selectedCluster.name', 'clustersToCreate.@each.name'),

  addCluster: function () {
    var self = this;
    var newClusterPopup = App.ModalPopup.show({
      header: Em.I18n.t('mirroring.manageClusters.create.cluster.popup'),
      bodyClass: Em.View.extend({
        controller: self,
        templateName: require('templates/main/mirroring/create_new_cluster')
      }),
      classNames: ['create-target-cluster-popup'],
      primary: Em.I18n.t('common.save'),
      secondary: Em.I18n.t('common.cancel'),
      onPrimary: function () {
        if (this.get('enablePrimary')) {
          this.set('enablePrimary', false);
          self.createNewCluster();
        }
      },
      willInsertElement: function () {
        var clusterName = App.get('clusterName');
        var newCluster = Ember.Object.create({
          name: '',
          execute: '',
          workflow: '',
          write: '',
          readonly: '',
          staging: '/apps/falcon/' + clusterName + '/staging',
          working: '/apps/falcon/' + clusterName + '/working',
          temp: '/tmp'
        });
        self.set('newCluster', newCluster);
      },
      didInsertElement: function () {
        this._super();
        this.fitHeight();
      }
    });
    this.set('newClusterPopup', newClusterPopup);
  },

  removeCluster: function () {
    var self = this;
    var selectedClusterName = self.get('selectedCluster.name');
    App.showConfirmationPopup(function () {
      App.ajax.send({
        name: 'mirroring.delete_entity',
        sender: self,
        data: {
          name: selectedClusterName,
          type: 'cluster',
          falconServer: App.get('falconServerURL')
        },
        success: 'onRemoveClusterSuccess',
        error: 'onRemoveClusterError'
      });
    }, Em.I18n.t('mirroring.manageClusters.remove.confirmation').format(selectedClusterName));
  },

  onRemoveClusterSuccess: function () {
    this.set('clusters', this.get('clusters').without(this.get('selectedCluster')));
  },

  onRemoveClusterError: function () {
    App.showAlertPopup(Em.I18n.t('common.error'), Em.I18n.t('mirroring.manageClusters.error') + ': ' + arguments[2]);
  },

  createNewCluster: function () {
    App.ajax.send({
      name: 'mirroring.submit_entity',
      sender: this,
      data: {
        type: 'cluster',
        entity: this.formatClusterXML(this.get('newCluster')),
        falconServer: App.get('falconServerURL')
      },
      success: 'onCreateClusterSuccess',
      error: 'onCreateClusterError'
    });
  },

  onCreateClusterSuccess: function () {
    this.get('clusters').pushObject(this.get('newCluster'));
    this.get('newClusterPopup').hide();
  },

  onCreateClusterError: function () {
    this.set('newClusterPopup.enablePrimary', true);
    App.showAlertPopup(Em.I18n.t('common.error'), Em.I18n.t('mirroring.manageClusters.error') + ': ' + arguments[2]);
  },

  /**
   * Return XML-formatted string made from cluster object
   * @param {Object} cluster - object with cluster data
   * @return {String}
   */
  formatClusterXML: function (cluster) {
    return '<?xml version="1.0"?><cluster colo="local" description="" name="' + cluster.get('name') +
        '" xmlns="uri:falcon:cluster:0.1"><interfaces><interface type="readonly" endpoint="' + cluster.get('readonly') +
        '" version="2.2.0" /><interface type="execute" endpoint="' + cluster.get('execute') +
        '" version="2.2.0" /><interface type="workflow" endpoint="' + cluster.get('workflow') +
        '" version="4.0.0" />' + '<interface type="messaging" endpoint="tcp://' + App.get('falconServerURL') + ':61616?daemon=true" version="5.1.6" />' +
        '<interface type="write" endpoint="' + cluster.get('write') + '" version="2.2.0" />' +
        '</interfaces><locations><location name="staging" path="' + cluster.get('staging') +
        '" /><location name="temp" path="' + cluster.get('temp') +
        '" /><location name="working" path="' + cluster.get('working') +
        '" /></locations></cluster>';
  }
});
