namespace com.sap.service.ItemScheduleLineModel;

using com.sap.service.common as Common from './common-model';
using com.sap.service.ItemModel from './item-model';
 
entity ItemScheduleLine{
	key UUID : UUID; 
	TypeCode : Common.ItemScheduleLineTypeCode;
	Quantity : Common.Quantity;
	StartDateTime : Timestamp;
	EndDateTime : Timestamp;
	
	Item : Association to ItemModel.Item;
} 