namespace com.sap.service.ItemModel;

using com.sap.service.common as Common from './common-model';
using com.sap.service.TicketModel from './ticket-model';
using com.sap.service.ItemScheduleLineModel from './itemScheduleLine-model';

entity Item{
	key UUID : UUID; 
	ItemID : String(10);
	Description : String(40);
	DescriptionLanguageCode : Common.LanguageCode; 
	UserServiceTransactionProcessingTypeCode : String(4);
	ServiceRequestExecutionLifeCycleStatusCode : String(2);
	
	Ticket : Association to TicketModel.Ticket;
	ItemScheduleLines : Association to many ItemScheduleLineModel.ItemScheduleLine on ItemScheduleLines.Item = $self;
}