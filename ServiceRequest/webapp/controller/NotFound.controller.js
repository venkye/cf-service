sap.ui.define([
		"zz/ServiceRequest/controller/BaseController"
	], function (BaseController) {
		"use strict";

		return BaseController.extend("zz.ServiceRequest.controller.NotFound", {

			/**
			 * Navigates to the worklist when the link is pressed
			 * @public
			 */
			onLinkPressed : function () {
				this.getRouter().navTo("worklist");
			}

		});

	}
);