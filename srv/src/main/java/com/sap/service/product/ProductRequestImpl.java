package com.sap.service.product;

import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductRequestImpl extends ProductRequest {
	private static final Logger logger = LoggerFactory.getLogger(ProductRequest.class.getName());

	public Map<String, Object> getProduct(String productID) {
		try {
			Edm edm = readEdm();
			ODataEntry entry = readEntry(edm, "A_Product", productID, "to_Description");
			Map<String, Object> propMap = entry.getProperties();
			return propMap;
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

		return null;
	}
}
