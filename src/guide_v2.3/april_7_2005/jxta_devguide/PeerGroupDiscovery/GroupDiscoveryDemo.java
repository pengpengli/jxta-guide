/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 */

import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.rendezvous.RendezVousService;


public class GroupDiscoveryDemo implements DiscoveryListener {

    static PeerGroup netPeerGroup  = null;
    private DiscoveryService discovery;
    private RendezVousService rdv;

    /**
     *  Method to start the JXTA platform.
     *  Waits until a connection to rdv is established.
     */

    private void startJxta() {
        try {
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();
        } catch ( PeerGroupException e) {

            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
        // Extract the discovery and rendezvous services from our peer group
        discovery = netPeerGroup.getDiscoveryService();
        rdv = netPeerGroup.getRendezVousService();

        // Wait until we connect to a rendezvous peer
        System.out.print("Waiting to connect to rendezvous...");
        while (! rdv.isConnectedToRendezVous()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                // nothing, keep going
            }
        }
        System.out.println("connected!");
    }

    /**
     * This thread loops forever discovering peers
     * every minute, and displaying the results.
     */

    public void run() {

        try {
            // Add ourselves as a DiscoveryListener for DiscoveryResponse events
            discovery.addDiscoveryListener(this);
            while (true) {
                System.out.println("Sending a Discovery Message");
                // look for any peer group
                discovery.getRemoteAdvertisements(null, DiscoveryService.GROUP,
                                                  null, null, 5);
                // wait a bit before sending next discovery message
                try {
                    Thread.sleep( 60 * 1000);
                } catch(Exception e) {}

            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * by implementing DiscoveryListener we must define this method
     * to deal to discovery responses 
     */

    public void discoveryEvent(DiscoveryEvent ev) {

        DiscoveryResponseMsg res = ev.getResponse();
        String name = "unknown";

        // Get the responding peer's advertisement
        PeerAdvertisement peerAdv = res.getPeerAdvertisement();
        // some peers may not respond with their peerAdv
        if (peerAdv != null) {
            name = peerAdv.getName();
        }
        System.out.println (" Got a Discovery Response [" +
                            res.getResponseCount()+ " elements]  from peer : " +
                            name);
        // now print out each discovered peer group
        PeerGroupAdvertisement adv = null;
        Enumeration en = res.getAdvertisements();

        if (en != null ) {
            while (en.hasMoreElements()) {
                adv = (PeerGroupAdvertisement) en.nextElement();
                System.out.println (" Peer Group = " + adv.getName());
            }
        }
    }
    static public void main(String args[]) {
        GroupDiscoveryDemo myapp  = new GroupDiscoveryDemo();
        myapp.startJxta();
        myapp.run();
    }

}
