/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja.parser.entities;

import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFPoint;
import org.kabeja.parser.DXFValue;

/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 */
public class DXFPointHandler extends AbstractEntityHandler {
  public static final String ENTITY_NAME = "POINT";
  private DXFPoint point;

  /** */
  public DXFPointHandler() {
    super();

    // TODO Auto-generated constructor stub
  }

  /*
   * (non-Javadoc)
   *
   * @see org.dxf2svg.parser.entities.EntityHandler#endParsing()
   */
  public void endDXFEntity() {}

  /*
   * (non-Javadoc)
   *
   * @see org.dxf2svg.parser.entities.EntityHandler#getEntity()
   */
  public DXFEntity getDXFEntity() {
    return point;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.dxf2svg.parser.entities.EntityHandler#getEntityName()
   */
  public String getDXFEntityName() {
    // TODO Auto-generated method stub
    return ENTITY_NAME;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.dxf2svg.parser.entities.EntityHandler#parseGroup(int,
   *      org.dxf2svg.parser.DXFValue)
   */
  public void parseGroup(int groupCode, DXFValue value) {

    switch (groupCode) {
      case GROUPCODE_START_X:
        point.setX(value.getDoubleValue());

        break;

      case GROUPCODE_START_Y:
        point.setY(value.getDoubleValue());

        break;

      case GROUPCODE_START_Z:
        point.setZ(value.getDoubleValue());

        break;

      default:
        super.parseCommonProperty(groupCode, value, point);

        break;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.dxf2svg.parser.entities.EntityHandler#startParsing()
   */
  public void startDXFEntity() {
    point = new DXFPoint();
    point.setDXFDocument(doc);
  }

  /* (non-Javadoc)
   * @see org.dxf2svg.parser.entities.EntityHandler#isFollowSequence()
   */
  public boolean isFollowSequence() {
    return false;
  }
}
