/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.junit.viewer.server;

import com.google.gwt.junit.viewer.client.Trial;

import org.w3c.dom.Element;

import java.util.List;
import java.util.Iterator;
import java.util.Map;

/**
 * Hydrates a benchmark Trial from an XML Element.
 *
 */
class TrialXml {

  public static Trial fromXml( Element element ) {
    Trial trial = new Trial();

    String timing = element.getAttribute( "timing" );

    if ( timing != null ) {
      trial.setRunTimeMillis(Double.parseDouble( timing ));
    }

    Element exception = ReportXml.getElementChild( element, "exception" );
    if ( exception != null ) {
      trial.setException( ReportXml.getText( exception ) );
    }

    List elements = ReportXml.getElementChildren( element, "variable" );

    Map variables = trial.getVariables();

    for ( Iterator it = elements.iterator(); it.hasNext(); ) {
      Element e = (Element) it.next();
      String name = e.getAttribute( "name" );
      String value = e.getAttribute( "value" );
      variables.put( name, value ) ;
    }
    
    return trial;
  }
}
