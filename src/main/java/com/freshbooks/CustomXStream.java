package com.freshbooks;


import java.util.Date;

import com.freshbooks.model.Autobill;
import com.freshbooks.model.Callback;
import com.freshbooks.model.Callbacks;
import com.freshbooks.model.Card;
import com.freshbooks.model.Categories;
import com.freshbooks.model.Category;
import com.freshbooks.model.Client;
import com.freshbooks.model.Clients;
import com.freshbooks.model.Credit;
import com.freshbooks.model.Expense;
import com.freshbooks.model.Expenses;
import com.freshbooks.model.Invoice;
import com.freshbooks.model.InvoiceLine;
import com.freshbooks.model.Item;
import com.freshbooks.model.Items;
import com.freshbooks.model.Links;
import com.freshbooks.model.Payment;
import com.freshbooks.model.Payments;
import com.freshbooks.model.Recurring;
import com.freshbooks.model.Recurrings;
import com.freshbooks.model.Request;
import com.freshbooks.model.Response;
import com.freshbooks.model.ResponseStatus;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class CustomXStream extends XStream {
    public CustomXStream() {
        super(null, new XppDriver(new XmlFriendlyReplacer("::", "_")));
        registerConverter(BooleanConverter.BINARY);
        registerConverter(new DateConverter("yyyy-MM-dd", new String[0]));
        registerConverter(new SingleValueConverter() {
            @SuppressWarnings("rawtypes")
			@Override
            public boolean canConvert(Class type) {
                return type.equals(Long.class);
            }
            
            @Override
            public Object fromString(String str) {
                if(str.isEmpty())
                    return null;
                return Long.valueOf(str, 10);
            }
            
            @Override
            public String toString(Object obj) {
                return obj.toString();
            }
        });
        registerConverter(new SingleValueConverter() {
            @SuppressWarnings("rawtypes")
			@Override
            public boolean canConvert(Class type) {
                return type.equals(Double.class);
            }
            
            @Override
            public Object fromString(String str) {
                if(str.isEmpty())
                    return null;
                return Double.valueOf(str);
            }
            
            @Override
            public String toString(Object obj) {
                return obj.toString();
            }
        });
        registerConverter(new DateConverter("yyyy-MM-dd", new String[0], true) {
            @Override
            public Object fromString(String str) {
                // FreshBooks returns these bogus dates sometimes ... no idea why.
                // We'll treat them as a "zero", but use 1970 Jan 1 instead
                if(str.startsWith("0000-")) {
                    return new Date(0);
                }
                return super.fromString(str);
            }
        });
        registerConverter(new Converter() {
			@SuppressWarnings("rawtypes")
			@Override
			public boolean canConvert(Class arg0) {
				return arg0.equals(Credit.class);
			}
			
			@Override
			public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
				Credit c = new Credit();
				c.setCurrency( reader.getAttribute("currency") );
				c.setAmount( Double.parseDouble(reader.getValue()) );
				return c;
			}
			
			@Override
			public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
				// TODO Auto-generated method stub
				final Credit c = (Credit)value;
				writer.addAttribute("currency", c.getCurrency());
				writer.setValue(String.valueOf(c.getAmount()));
			}
		});
        processAnnotations(new Class[] {
            Request.class,
            Response.class,
            ResponseStatus.class,
            Invoice.class,
            InvoiceLine.class,
            Client.class,
            Clients.class,
            Item.class,
            Items.class,
            Category.class,
            Categories.class,
            Callback.class,
            Callbacks.class,
            Expense.class,
            Expenses.class,
            Recurring.class,
            Recurrings.class,
            Payment.class,
            Payments.class,
            Item.class,
            Items.class,
            Links.class,
            Autobill.class,
            Exception.class,
            Card.class,
            Credit.class
        });
    }
    
    /**
     * Allow and ignore unexpected xml tags
     */  
    protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
            @SuppressWarnings("rawtypes")
			public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                try {
                	//check wrapped mapper first so that omitField will still work
                	if ( !super.shouldSerializeMember(definedIn, fieldName) ) {
                		return false;
                	}
                    return definedIn != Object.class || realClass(fieldName) != null;
                } catch(CannotResolveClassException cnrce) {
                    return false;
                }
            }
        };
    }
    
    
}
