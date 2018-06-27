using com.sap.service.TicketModel from '../db/ticket-model';
using com.sap.service.PartyModel from '../db/party-model';
using com.sap.service.ItemModel from '../db/item-model';
using com.sap.service.ItemScheduleLineModel from '../db/itemScheduleLine-model';
using com.sap.service.ServiceRefObjectModel from '../db/serviceRefObject-model';

service TicketService{
	type productRet {
	    Product: String;
	    ProductType: String;
	    ProductDescription: String;
	  }
  
	entity Ticket as projection on TicketModel.Ticket;
	entity Party as projection on PartyModel.Party;
	entity Item as projection on ItemModel.Item;
	entity ItemScheduleLine as projection on ItemScheduleLineModel.ItemScheduleLine;
	entity ServiceRefObject as projection on ServiceRefObjectModel.ServiceRefObject; 
	
	function getProductInfo(ProductID:String) returns productRet; 
}