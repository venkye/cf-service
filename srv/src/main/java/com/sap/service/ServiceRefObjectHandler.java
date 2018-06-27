package com.sap.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.EntityDataBuilder;
import com.sap.cloud.sdk.service.prov.api.ExtensionHelper;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeCreate;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeCreateResponse;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.service.product.ProductRequestImpl;

import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;

public class ServiceRefObjectHandler {
	@BeforeCreate(entity = "ServiceRefObject", serviceName = "TicketService")
	public BeforeCreateResponse beforeCreateServiceRefObject(CreateRequest cr, ExtensionHelper eh) {
		EntityData ed = cr.getData();
		EntityData edNew = null;

		String productID = (String) ed.getElementValue("ProductID");
		if (productID == null || productID.isEmpty())
			return BeforeCreateResponse
					.setError(ErrorResponse.getBuilder().setMessage("Invlalid product ID").response());

		Map<String, Object> product = new ProductRequestImpl().getProduct(productID);
		if (product == null)
			return BeforeCreateResponse
					.setError(ErrorResponse.getBuilder().setMessage("Invlalid product ID").response());

		String productDesc = null;
		String productType = (String) product.get("ProductType");
		Object inlineFeed = product.get("to_Description");
		if (inlineFeed instanceof ODataDeltaFeed) {
			List<ODataEntry> entries = ((ODataDeltaFeed) inlineFeed).getEntries();
			for (ODataEntry entry : entries) {
				for (Map.Entry<String, Object> e : entry.getProperties().entrySet()) {
					if (e.getKey().equals("ProductDescription")) {
						productDesc = (String) e.getValue();
						break;
					}
				}
			}
		}

		EntityDataBuilder eb = EntityData.getBuilder(ed);
		eb.addElement("ProductDesc", productDesc);
		eb.addElement("ProductType", productType);
		eb.addElement("UUID", UUID.randomUUID().toString());
		edNew = eb.buildEntityData("ServiceRefObject");

		return BeforeCreateResponse.setSuccess().setEntityData(edNew).response();
	}
}
