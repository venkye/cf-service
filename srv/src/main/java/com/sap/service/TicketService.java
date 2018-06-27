package com.sap.service;

import java.util.Arrays;
import java.util.Map;

import com.sap.cloud.sdk.service.prov.api.ExtensionHelper;
import com.sap.cloud.sdk.service.prov.api.annotations.ExtendFunction;
import com.sap.cloud.sdk.service.prov.api.request.OperationRequest;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.api.response.OperationResponse;
import com.sap.service.product.ProductRequestImpl;

public class TicketService {
	@ExtendFunction(Name = "getProductInfo", serviceName = "TicketService")
	public OperationResponse getProductInfo(OperationRequest functionRequest, ExtensionHelper extensionHelper) {
		Map<String, Object> params = functionRequest.getParameters();
		try {
			String productID = (String) params.get("ProductID");

			Map<String, Object> product = new ProductRequestImpl().getProduct(productID);
			OperationResponse response = OperationResponse.setSuccess().setComplexData(Arrays.asList(product))
					.response();

			return response;
		} catch (Exception e) {
			ErrorResponse errorResponse = ErrorResponse.getBuilder().setMessage(e.getMessage()).setCause(e).response();
			return OperationResponse.setError(errorResponse);
		}
	}
}
