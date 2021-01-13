/* Licensed under the Apache License, Version 2.0 (the "License");
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

/*
 * Task listeners
 */

angular.module('flowableModeler').controller('FlowableReviewConditionsCtrl',
    ['$scope', '$modal', '$timeout', '$translate', function ($scope, $modal, $timeout, $translate) {

        // Config for the modal window
        var opts = {
            template: 'editor-app/configuration/properties/review-conditions-popup.html?version=' + Date.now(),
            scope: $scope
        };

        // Open the dialog
        _internalCreateModal(opts, $modal, $scope);
    }]);

angular.module('flowableModeler').controller('FlowableReviewConditionsPopupCtrl',
    ['$scope', '$q', '$translate', '$timeout', function ($scope, $q, $translate, $timeout) {

        // Put json representing form properties on scope
        if ($scope.property.value !== undefined && $scope.property.value !== null
            && $scope.property.value.reviewConditions !== undefined
            && $scope.property.value.reviewConditions !== null) {

            if ($scope.property.value.reviewConditions.constructor == String) {
                $scope.reviewConditions = JSON.parse($scope.property.value.reviewConditions);
            } else {
                // Note that we clone the json object rather then setting it directly,
                // this to cope with the fact that the user can click the cancel button and no changes should have happened
                $scope.reviewConditions = angular.copy($scope.property.value.reviewConditions);
            }

            for (var i = 0; i < $scope.reviewConditions.length; i++) {
                var reviewCondition = $scope.reviewConditions[i];
                if (reviewCondition.className !== undefined && reviewCondition.className !== '') {
                    reviewCondition.implementation = reviewCondition.className;
                } else if (reviewCondition.expression !== undefined && reviewCondition.expression !== '') {
                    reviewCondition.implementation = reviewCondition.expression;
                } else if (reviewCondition.delegateExpression !== undefined && reviewCondition.delegateExpression !== '') {
                    reviewCondition.implementation = reviewCondition.delegateExpression;
                }
            }
        } else {
            $scope.reviewConditions = [];
        }

        $scope.selectedListener = undefined;
        $scope.selectedField = undefined;
        $scope.fields = [];
        $scope.translationsRetrieved = false;

        $scope.labels = {};

        var eventPromise = $translate('PROPERTY.REVIEWCONDITIONS.EVENT');
        var NAMEPromise = $translate('PROPERTY.REVIEWCONDITIONS.NAME');
        var implementationPromise = $translate('PROPERTY.REVIEWCONDITIONS.FIELDS.IMPLEMENTATION');
        var namePromise = $translate('PROPERTY.REVIEWCONDITIONS.FIELDS.NAME');

        $q.all([eventPromise, implementationPromise, namePromise, NAMEPromise]).then(function (results) {
            $scope.labels.eventLabel = results[0];
            $scope.labels.implementationLabel = results[1];
            $scope.labels.nameLabel = results[2];
            $scope.labels.NAMELabel = results[3];
            $scope.translationsRetrieved = true;

            // Config for grid
            $scope.gridOptions = {
                data: $scope.reviewConditions,
                headerRowHeight: 28,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                enableHorizontalScrollbar: 0,
                enableColumnMenus: false,
                enableSorting: false,
                columnDefs: [
                    {field: 'name', displayName: $scope.labels.NAMELabel},
                    {field: 'event', displayName: $scope.labels.eventLabel},
                    {field: 'implementation', displayName: $scope.labels.implementationLabel}
                ]
            };

            $scope.gridOptions.onRegisterApi = function (gridApi) {
                //set gridApi on scope
                $scope.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    $scope.selectedListener = row.entity;
                    $scope.selectedField = undefined;
                    if ($scope.selectedListener) {
                        var fields = $scope.selectedListener.fields;
                        if (fields !== undefined && fields !== null) {
                            for (var i = 0; i < fields.length; i++) {
                                var field = fields[i];
                                if (field.stringValue !== undefined && field.stringValue !== '') {
                                    field.implementation = field.stringValue;
                                } else if (field.expression !== undefined && field.expression !== '') {
                                    field.implementation = field.expression;
                                } else if (field.string !== undefined && field.string !== '') {
                                    field.implementation = field.string;
                                }
                            }
                        } else {
                            $scope.selectedListener.fields = [];
                        }

                        $scope.fields.length = 0;
                        for (var i = 0; i < $scope.selectedListener.fields.length; i++) {
                            $scope.fields.push($scope.selectedListener.fields[i]);
                        }
                    }
                });
            };

            // Config for field grid
            $scope.gridFieldOptions = {
                data: $scope.fields,
                headerRowHeight: 28,
                enableRowSelection: true,
                enableRowHeaderSelection: false,
                multiSelect: false,
                modifierKeysToMultiSelect: false,
                columnDefs: [{field: 'name', displayName: $scope.labels.nameLabel},
                    {field: 'implementation', displayName: $scope.labels.implementationLabel}]
            };

            $scope.gridFieldOptions.onRegisterApi = function (gridApi) {
                // set gridApi on scope
                $scope.fieldGridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                    $scope.selectedField = row.entity;
                });
            };
        });

        $scope.listenerDetailsChanged = function () {
            if ($scope.selectedListener.className !== '') {
                $scope.selectedListener.implementation = $scope.selectedListener.className;
            } else if ($scope.selectedListener.expression !== '') {
                $scope.selectedListener.implementation = $scope.selectedListener.expression;
            } else if ($scope.selectedListener.delegateExpression !== '') {
                $scope.selectedListener.implementation = $scope.selectedListener.delegateExpression;
            } else {
                $scope.selectedListener.implementation = '';
            }
        };

        // Click handler for add button
        $scope.addNewListener = function () {
            var newListener = {
                event: '1',
                implementation: '',
                className: '',
                expression: '',
                delegateExpression: ''
            };
            $scope.reviewConditions.push(newListener);

            $timeout(function () {
                $scope.gridApi.selection.toggleRowSelection(newListener);
            });
        };

        // Click handler for remove button
        $scope.removeListener = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.reviewConditions.indexOf(selectedItems[0]);
                $scope.gridApi.selection.toggleRowSelection(selectedItems[0]);

                $scope.reviewConditions.splice(index, 1);

                if ($scope.reviewConditions.length == 0) {
                    $scope.selectedListener = undefined;
                }

                $timeout(function () {
                    if ($scope.reviewConditions.length > 0) {
                        $scope.gridApi.selection.toggleRowSelection($scope.reviewConditions[0]);
                    }
                });
            }
        };

        // Click handler for up button
        $scope.moveListenerUp = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.reviewConditions.indexOf(selectedItems[0]);
                if (index != 0) { // If it's the first, no moving up of course
                    var temp = $scope.reviewConditions[index];
                    $scope.reviewConditions.splice(index, 1);
                    $timeout(function () {
                        $scope.reviewConditions.splice(index + -1, 0, temp);
                        $timeout(function () {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for down button
        $scope.moveListenerDown = function () {
            var selectedItems = $scope.gridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.reviewConditions.indexOf(selectedItems[0]);
                if (index != $scope.reviewConditions.length - 1) { // If it's the last element, no moving down of course
                    var temp = $scope.reviewConditions[index];
                    $scope.reviewConditions.splice(index, 1);
                    $timeout(function () {
                        $scope.reviewConditions.splice(index + 1, 0, temp);
                        $timeout(function () {
                            $scope.gridApi.selection.toggleRowSelection(temp);
                        });
                    });

                }
            }
        };

        $scope.fieldDetailsChanged = function () {
            if ($scope.selectedField.stringValue != '') {
                $scope.selectedField.implementation = $scope.selectedField.stringValue;
            } else if ($scope.selectedField.expression != '') {
                $scope.selectedField.implementation = $scope.selectedField.expression;
            } else if ($scope.selectedField.string != '') {
                $scope.selectedField.implementation = $scope.selectedField.string;
            } else {
                $scope.selectedField.implementation = '';
            }
        };

        // Click handler for add button
        $scope.addNewField = function () {
            if ($scope.selectedListener) {
                if ($scope.selectedListener.fields == undefined) {
                    $scope.selectedListener.fields = [];
                }

                var newField = {
                    name: 'fieldName',
                    implementation: '',
                    stringValue: '',
                    expression: '',
                    string: ''
                };
                $scope.fields.push(newField);
                $scope.selectedListener.fields.push(newField);

                $timeout(function () {
                    $scope.fieldGridApi.selection.toggleRowSelection(newField);
                });
            }
        };

        // Click handler for remove button
        $scope.removeField = function () {
            var selectedItems = $scope.fieldGridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.fields.indexOf(selectedItems[0]);
                $scope.fieldGridApi.selection.toggleRowSelection(selectedItems[0]);

                $scope.fields.splice(index, 1);
                $scope.selectedListener.fields.splice(index, 1);

                if ($scope.fields.length == 0) {
                    $scope.selectedField = undefined;
                }

                $timeout(function () {
                    if ($scope.fields.length > 0) {
                        $scope.fieldGridApi.selection.toggleRowSelection($scope.fields[0]);
                    }
                });
            }
        };

        // Click handler for up button
        $scope.moveFieldUp = function () {
            var selectedItems = $scope.fieldGridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.fields.indexOf(selectedItems[0]);
                if (index != 0) { // If it's the first, no moving up of course
                    var temp = $scope.fields[index];
                    $scope.fields.splice(index, 1);
                    $scope.selectedListener.fields.splice(index, 1);
                    $timeout(function () {
                        $scope.fields.splice(index + -1, 0, temp);
                        $scope.selectedListener.fields.splice(index + -1, 0, temp);
                        $timeout(function () {
                            $scope.fieldGridApi.selection.toggleRowSelection(temp);
                        });
                    });

                }
            }
        };

        // Click handler for down button
        $scope.moveFieldDown = function () {
            var selectedItems = $scope.fieldGridApi.selection.getSelectedRows();
            if (selectedItems && selectedItems.length > 0) {
                var index = $scope.fields.indexOf(selectedItems[0]);
                if (index != $scope.fields.length - 1) { // If it's the last element, no moving down of course
                    var temp = $scope.fields[index];
                    $scope.fields.splice(index, 1);
                    $scope.selectedListeners.fields.splice(index, 1);
                    $timeout(function () {
                        $scope.fields.splice(index + 1, 0, temp);
                        $scope.selectedListener.fields.splice(index + 1, 0, temp);
                        $timeout(function () {
                            $scope.fieldGridApi.selection.toggleRowSelection(temp);
                        });
                    });
                }
            }
        };

        // Click handler for save button
        $scope.save = function () {

            if ($scope.reviewConditions.length > 0) {
                $scope.property.value = {};
                $scope.property.value.reviewConditions = $scope.reviewConditions;
            } else {
                $scope.property.value = null;
            }

            $scope.updatePropertyInModel($scope.property);
            $scope.close();
        };

        $scope.cancel = function () {
            $scope.close();
        };

        // Close button handler
        $scope.close = function () {
            $scope.property.mode = 'read';
            $scope.$hide();
        };

    }]);
