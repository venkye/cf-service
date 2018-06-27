/*global QUnit*/

jQuery.sap.require("sap.ui.qunit.qunit-css");
jQuery.sap.require("sap.ui.thirdparty.qunit");
jQuery.sap.require("sap.ui.qunit.qunit-junit");
QUnit.config.autostart = false;

sap.ui.require([
	"sap/ui/test/Opa5",
	"zz/ServiceRequest/test/integration/pages/Common",
	"sap/ui/test/opaQunit",
	"zz/ServiceRequest/test/integration/pages/Worklist",
	"zz/ServiceRequest/test/integration/pages/Object",
	"zz/ServiceRequest/test/integration/pages/NotFound",
	"zz/ServiceRequest/test/integration/pages/Browser",
	"zz/ServiceRequest/test/integration/pages/App"
], function (Opa5, Common) {
	"use strict";
	Opa5.extendConfig({
		arrangements: new Common(),
		viewNamespace: "zz.ServiceRequest.view."
	});

	sap.ui.require([
		"zz/ServiceRequest/test/integration/WorklistJourney",
		"zz/ServiceRequest/test/integration/ObjectJourney",
		"zz/ServiceRequest/test/integration/NavigationJourney",
		"zz/ServiceRequest/test/integration/NotFoundJourney"
	], function () {
		QUnit.start();
	});
});