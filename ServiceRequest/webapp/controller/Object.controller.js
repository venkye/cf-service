/*global location*/
sap.ui.define([
	"zz/ServiceRequest/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/core/routing/History",
	"zz/ServiceRequest/model/formatter",
	"sap/ui/model/odata/type/Guid"
], function(
	BaseController,
	JSONModel,
	History,
	formatter,
	Guid
) {
	"use strict";

	return BaseController.extend("zz.ServiceRequest.controller.Object", {

		formatter: formatter,

		/* =========================================================== */
		/* lifecycle methods                                           */
		/* =========================================================== */

		/**
		 * Called when the worklist controller is instantiated.
		 * @public
		 */
		onInit: function() {
			// Model used to manipulate control states. The chosen values make sure,
			// detail page is busy indication immediately so there is no break in
			// between the busy indication for loading the view's meta data
			var iOriginalBusyDelay,
				oViewModel = new JSONModel({
					busy: true,
					delay: 0
				});
			var oPayloadModel = new JSONModel();

			this.getRouter().getRoute("object").attachPatternMatched(this._onObjectMatched, this);

			// Store original busy indicator delay, so it can be restored later on
			iOriginalBusyDelay = this.getView().getBusyIndicatorDelay();
			this.setModel(oViewModel, "objectView");
			this.setModel(oPayloadModel, "oPayloadModel");
			this.getOwnerComponent().getModel().metadataLoaded().then(function() {
				// Restore original busy indicator delay for the object view
				oViewModel.setProperty("/delay", iOriginalBusyDelay);
			});
		},

		/* =========================================================== */
		/* event handlers                                              */
		/* =========================================================== */

		/**
		 * Event handler  for navigating back.
		 * It there is a history entry we go one step back in the browser history
		 * If not, it will replace the current entry of the browser history with the worklist route.
		 * @public
		 */
		onNavBack: function() {
			var sPreviousHash = History.getInstance().getPreviousHash();

			if (sPreviousHash !== undefined) {
				history.go(-1);
			} else {
				this.getRouter().navTo("worklist", {}, true);
			}
		},

		openProductCreate: function(oEvent) {
			var oPayloadModel = this.getModel("oPayloadModel");
			oPayloadModel.setData({
				path: "/ServiceRefObjects",
				payload: '{\n\t"ProductID" : "",\n\t"MainIndicator" : false\n}'
			});
			this._showPopOver(oEvent, "Right");
			oPayloadModel.updateBindings();
		},

		deleteProduct: function() {
			var oItem = this.getView().byId("productList").getSelectedItem();
			var sPath = oItem.getBindingContext().getPath();
			var oDataModel = this.getModel();

			oDataModel.remove(sPath);
		},

		getProductInfo: function(oEvent) {
			var oItem = this.getView().byId("productList").getSelectedItem();
			var sPath = "/getProductInfo";
			var oModel = this.getModel();
			oModel.callFunction(sPath, {
				"method": "GET",
				"urlParameters": {
					"ProductID": oItem.getBindingContext().getProperty("ProductID")
				},
				success: function(oData, oResponse) {
					sap.m.MessageBox.success(JSON.stringify(JSON.parse(oResponse.body), null, '\t'));
				},
				error: function(oError) {
					sap.m.MessageBox.error(oError);
				}
			});
		},

		onCreate: function(oEvent) {
			var sPath = this.getView().getBindingContext().getPath();
			var oPayload = this.getModel("oPayloadModel").getData();
			var oDataModel = this.getModel();
			var oEntity = JSON.parse(oPayload.payload);
			oEntity.Ticket_UUID = this.getView().getBindingContext().getObject().UUID;

			oDataModel.create(sPath + oPayload.path, oEntity);
			this._oPopover.close();
		},

		/* =========================================================== */
		/* internal methods                                            */
		/* =========================================================== */

		/**
		 * Binds the view to the object path.
		 * @function
		 * @param {sap.ui.base.Event} oEvent pattern match event in route 'object'
		 * @private
		 */
		_showPopOver: function(oEvent, sPlacement) {
			if (!this._oPopover) {
				this._oPopover = sap.ui.xmlfragment("zz.ServiceRequest.view.QuickCreate", this);
				this._oPopover.setPlacement(sPlacement);
				this.getView().addDependent(this._oPopover);
			}
			this._oPopover.openBy(oEvent.getSource());
		},
		_onObjectMatched: function(oEvent) {
			var sObjectId = oEvent.getParameter("arguments").objectId;
			this.getModel().metadataLoaded().then(function() {
				var sObjectPath = this.getModel().createKey("Ticket", {
					UUID: sObjectId
				});
				this._bindView("/" + sObjectPath);
			}.bind(this));
		},

		/**
		 * Binds the view to the object path.
		 * @function
		 * @param {string} sObjectPath path to the object to be bound
		 * @private
		 */
		_bindView: function(sObjectPath) {
			var oViewModel = this.getModel("objectView"),
				oDataModel = this.getModel();

			this.getView().bindElement({
				path: sObjectPath,
				events: {
					change: this._onBindingChange.bind(this),
					dataRequested: function() {
						oDataModel.metadataLoaded().then(function() {
							// Busy indicator on view should only be set if metadata is loaded,
							// otherwise there may be two busy indications next to each other on the
							// screen. This happens because route matched handler already calls '_bindView'
							// while metadata is loaded.
							oViewModel.setProperty("/busy", true);
						});
					},
					dataReceived: function() {
						oViewModel.setProperty("/busy", false);
					}
				}
			});
		},

		_onBindingChange: function() {
			var oView = this.getView(),
				oViewModel = this.getModel("objectView"),
				oElementBinding = oView.getElementBinding();

			// No data for the binding
			if (!oElementBinding.getBoundContext()) {
				this.getRouter().getTargets().display("objectNotFound");
				return;
			}

			var oResourceBundle = this.getResourceBundle(),
				oObject = oView.getBindingContext().getObject(),
				sObjectId = oObject.UUID,
				sObjectName = oObject.Subject;

			oViewModel.setProperty("/busy", false);

			oViewModel.setProperty("/shareSendEmailSubject",
				oResourceBundle.getText("shareSendEmailObjectSubject", [sObjectId]));
			oViewModel.setProperty("/shareSendEmailMessage",
				oResourceBundle.getText("shareSendEmailObjectMessage", [sObjectName, sObjectId, location.href]));
		},

		_generateGuid: function() {
			var d = new Date().getTime();
			var uuid = 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx'.replace(/[x]/g, function(c) {
				var r = (d + Math.random() * 16) % 16 | 0;
				d = Math.floor(d / 16);
				return (c === 'x' ? r : (r & 0x7 | 0x8)).toString(16);
			});
			return new Guid().parseValue(uuid, "string");
		}

	});

});