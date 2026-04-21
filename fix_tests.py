import os

services = ['movie-service', 'theater-service', 'showtime-service', 'booking-service', 'payment-service']

h2_dep_old = """
		<!-- H2 Database for tests -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>test</scope>
		</dependency>
"""

h2_dep_new = """
		<!-- H2 Database for tests -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>test</scope>
			<version>2.3.232</version>
		</dependency>
"""

for service in services:
    pom_path = f"{service}/pom.xml"
    if os.path.exists(pom_path):
        with open(pom_path, "r") as f:
            content = f.read()
        
        if "com.h2database" in content and "<version>" not in content.split("<artifactId>h2</artifactId>")[1].split("</dependency>")[0]:
            content = content.replace(h2_dep_old, h2_dep_new)
            with open(pom_path, "w") as f:
                f.write(content)

print("POMs updated")
