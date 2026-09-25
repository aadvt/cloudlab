package org.cloudbus.cloudsim.examples;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
public class Example {
public static void main(String[] args) {
try {
// STEP 1: Initialize CloudSim
int numUsers = 1;
Calendar calendar =
Calendar.getInstance();
boolean traceFlag = false;
CloudSim.init(
numUsers,
calendar,
traceFlag
);
// STEP 2: Create Data Center
Datacenter datacenter =
createDatacenter(
&quot;Datacenter_0&quot;
);
// STEP 3: Create Broker
DatacenterBroker broker =
new DatacenterBroker(

&quot;Broker&quot;
);
int brokerId =
broker.getId();
// STEP 4: Create VMs
List&lt;Vm&gt; vmList =
createVMs(brokerId);
broker.submitVmList(vmList);
// STEP 5: Create Cloudlets
List&lt;Cloudlet&gt; cloudletList =
createCloudlets(brokerId);
// STEP 6: Apply NEW algorithm
List&lt;Cloudlet&gt; scheduledCloudlets =
shortestCloudletFirst(
cloudletList
);
// Display scheduling order
System.out.println(
&quot;\nSCF Scheduling Order:&quot;
);
for (Cloudlet cloudlet :
scheduledCloudlets) {
System.out.println(
&quot;Cloudlet &quot;
+ cloudlet.getCloudletId()
+ &quot; - Length: &quot;
+ cloudlet.getCloudletLength()
);
}
// STEP 7: Submit cloudlets
broker.submitCloudletList(
scheduledCloudlets
);
// STEP 8: Start simulation
CloudSim.startSimulation();
// STEP 9: Get results
List&lt;Cloudlet&gt; resultList =
broker.getCloudletReceivedList();
CloudSim.stopSimulation();
// STEP 10: Print results
System.out.println(
&quot;\n========== RESULTS ==========&quot;
);
System.out.println(
&quot;Cloudlet\tStatus\tVM\tStart\tFinish&quot;

);
for (Cloudlet cloudlet :
resultList) {
System.out.println(
cloudlet.getCloudletId()
+ &quot;\t\t&quot;
+ cloudlet.getCloudletStatusString()
+ &quot;\t&quot;
+ cloudlet.getVmId()
+ &quot;\t&quot;
+ cloudlet.getExecStartTime()
+ &quot;\t&quot;
+ cloudlet.getFinishTime()
);
}
} catch (Exception e) {
e.printStackTrace();
}
}
// =====================================
// CREATE DATA CENTER
// =====================================
private static Datacenter createDatacenter(
String name) throws Exception {
List&lt;Host&gt; hostList =
new ArrayList&lt;Host&gt;();
// Create CPU
List&lt;Pe&gt; peList =
new ArrayList&lt;Pe&gt;();
int mips = 1000;
peList.add(
new Pe(
0,
new PeProvisionerSimple(
mips
)
)
);
// Create Physical Host
Host host =
new Host(
0,
new RamProvisionerSimple(
2048
),
new BwProvisionerSimple(
10000

),
1000000,
peList,
new VmSchedulerTimeShared(
peList
)
);
hostList.add(host);
// Data Center characteristics
String arch = &quot;x86&quot;;
String os = &quot;Linux&quot;;
String vmm = &quot;Xen&quot;;
double timeZone = 10.0;
double cost = 3.0;
double costPerMem = 0.05;
double costPerStorage = 0.001;
double costPerBw = 0.0;
LinkedList&lt;Storage&gt; storageList =
new LinkedList&lt;Storage&gt;();
DatacenterCharacteristics characteristics =
new DatacenterCharacteristics(
arch,
os,
vmm,
hostList,
timeZone,
cost,
costPerMem,
costPerStorage,
costPerBw
);
// Create Data Center
return new Datacenter(
name,
characteristics,
new VmAllocationPolicySimple(
hostList
),
storageList,
0
);
}
// =====================================
// CREATE VMs
// =====================================
private static List&lt;Vm&gt; createVMs(
int brokerId) {
List&lt;Vm&gt; vmList =
new ArrayList&lt;Vm&gt;();

int mips = 1000;
int ram = 512;
long bw = 1000;
long size = 10000;
int pesNumber = 1;
String vmm = &quot;Xen&quot;;
for (int i = 0; i &lt; 2; i++) {
Vm vm =
new Vm(
i,
brokerId,
mips,
pesNumber,
ram,
bw,
size,
vmm,
new CloudletSchedulerTimeShared()
);
vmList.add(vm);
}
return vmList;
}
// =====================================
// CREATE CLOUDLETS
// =====================================
private static List&lt;Cloudlet&gt; createCloudlets(
int brokerId) {
List&lt;Cloudlet&gt; cloudletList =
new ArrayList&lt;Cloudlet&gt;();
long[] lengths = {
8000,
2000,
5000,
1000,
7000,
3000
};
int pesNumber = 1;
long fileSize = 300;
long outputSize = 300;
UtilizationModel utilizationModel =
new UtilizationModelFull();
for (int i = 0;
i &lt; lengths.length;
i++) {

Cloudlet cloudlet =
new Cloudlet(
i,
lengths[i],
pesNumber,
fileSize,
outputSize,
utilizationModel,
utilizationModel,
utilizationModel
);
cloudlet.setUserId(brokerId);
cloudletList.add(cloudlet);
}
return cloudletList;
}
// =====================================
// NEW SCHEDULING ALGORITHM
// SHORTEST CLOUDLET FIRST
// =====================================
private static List&lt;Cloudlet&gt;
shortestCloudletFirst(
List&lt;Cloudlet&gt; cloudlets) {
List&lt;Cloudlet&gt; sortedCloudlets =
new ArrayList&lt;Cloudlet&gt;(
cloudlets
);
Collections.sort(
sortedCloudlets,
new Comparator&lt;Cloudlet&gt;() {
@Override
public int compare(
Cloudlet c1,
Cloudlet c2) {
return Long.compare(
c1.getCloudletLength(),
c2.getCloudletLength()
);
}
}
);
return sortedCloudlets;
}
}
