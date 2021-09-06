/*
   Copyright 2008 Simon Mieth

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
package org.kabeja.ui.model;

import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.TreeNode;


public class PropertiesTreeNode extends AbstractProcessingTreeNode {
    protected final static String LABEL = "Properties";
    protected Map properties;

    public PropertiesTreeNode(TreeNode parent, Map properties) {
        super(parent, LABEL);
        this.properties = properties;
    }

    protected void initializeChildren() {
        Iterator i = this.properties.keySet().iterator();

        while (i.hasNext()) {
            this.addChild(new PropertyTreeNode(this, properties,
                    (String) i.next()));
        }
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return false;
    }
}
