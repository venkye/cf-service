namespace com.sap.service.common;

type Amount {
	value : Decimal(10,3);
	currency : String;
}
type Quantity {
	value : Integer;
	unit : String;
}
type PriorityCode : String(1) enum {
	Immediate = 1;
	Urgent = 2;
	Normal = 3;
	Low = 7;
}
type LanguageCode : String(2);

type ItemScheduleLineTypeCode : String(2) enum {
	Requested = 1;
	Confirmed = 2; 
	Fulfilled = 5;	
}

type PartyID : String(60);
