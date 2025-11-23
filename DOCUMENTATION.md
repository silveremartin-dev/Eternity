# Eternity II - Documentation Map

This document provides a comprehensive guide to all documentation in the Eternity II project.

## 📚 Documentation Structure

### 1. User Documentation

#### **README.md** 
Location: `./README.md`  
Purpose: Quick start guide and feature overview  
Audience: New users and developers

**Contents:**
- Quick start commands
- Feature list (server/client)
- Basic architecture diagram
- Development commands
- Project structure
- Configuration files
- License and author info

#### **readme.txt**
Location: `./readme.txt`  
Purpose: Original project description  
Audience: General users

**Contents:**
- Game description
- Official and fan websites
- License information
- Release date

### 2. Architecture Documentation

#### **ARCHITECTURE.md** ✨ *NEW*
Location: `./ARCHITECTURE.md`  
Purpose: Comprehensive system architecture  
Audience: Developers and architects

**Contents:**
- System overview and diagrams
- Component descriptions (Server, Client, Domain)
- Communication protocols
- Data flow diagrams
- Design patterns
- Threading model
- Configuration details
- Future enhancements

### 3. Code Documentation

#### **Package Documentation** (`package-info.java`)
Location: Multiple packages in `src/main/java/`

Available in:
- `org.game.eternity2/package-info.java` - Root package
- `org.game.eternity2.client/package-info.java` - Client components
- `org.game.eternity2.server/package-info.java` - Server components
- `org.game.eternity2.elements/package-info.java` - Domain model
- `org.game.eternity2.elements.size4x4/package-info.java` - 4x4 puzzle
- `org.game.eternity2.elements.size6x6/package-info.java` - 6x6 puzzle
- `org.game.eternity2.elements.size12x6/package-info.java` - 12x6 puzzle
- `org.game.eternity2.elements.size16x16/package-info.java` - 16x16 puzzle
- `org.game.eternity2.io/package-info.java` - I/O and persistence

#### **JavaDoc** ✨ *GENERATED*
Location: `target/site/apidocs/`  
Purpose: API documentation  
Audience: Developers

**How to access:**
1. Open `target/site/apidocs/index.html` in browser
2. Or regenerate with: `mvn javadoc:javadoc`

**Note:** Generated with 100 warnings indicating missing JavaDoc comments in some methods. These can be addressed in future updates.

### 4. Research Materials

#### **Papers Directory**
Location: `./papers/`  
Purpose: Academic research on Eternity II solving  
Audience: Researchers and advanced developers

**13 Research Papers:**
1. `1709.00252.pdf`
2. `2007-08.projet.eternityII.rapport03.sdd.pdf`
3. `2008_06_26_semVAG_EBourreau.pdf`
4. `Eternity II puzzle (pieces and board).pdf`
5. `Eternity-UK instructions.pdf`
6. `Fast_Global_Filtering_for_Eternity_II.pdf`
7. `Main puzzle solution sheet.pdf`
8. `document.pdf`
9. `eii_details.pdf`
10. `eternity.pdf`
11. `metajmma12_submission_6.pdf`
12. `patey-e2-rapport.pdf`
13. `rapportCuvillier.pdf`

### 5. Configuration Documentation

#### **Server Configuration**
Location: `src/main/resources/server-config.properties`  
Purpose: Server runtime configuration

#### **Client Configuration**
Location: `src/main/resources/client-config.properties`  
Purpose: Client runtime configuration

#### **Logging Configuration**
Location: `src/main/resources/log4j2.xml`  
Purpose: Log4j2 logging setup  
Features:
- Rolling file appenders
- Separate server/client logs
- Timestamped log files in `logs/` directory

### 6. Build Documentation

#### **Maven POM**
Location: `./pom.xml`  
Purpose: Build configuration and dependencies

**Key sections:**
- Dependencies (JavaFX, Log4j2, Gson, WebSocket)
- Build plugins (Compiler, Exec)
- Project metadata

#### **Build Scripts**
- `launch_system.ps1` - PowerShell launch script
- `kill_system.ps1` - PowerShell kill script
- `launch-system.bat` - Batch launch script

## 🔍 Quick Reference

### For New Users
Start with: `README.md`

### For Developers
1. Read `README.md` for overview
2. Review `ARCHITECTURE.md` for system design
3. Browse JavaDoc at `target/site/apidocs/`
4. Check `package-info.java` for package-level docs

### For Researchers
Explore `papers/` directory for academic research

### For System Administrators
Review:
- `ARCHITECTURE.md` (Configuration section)
- `server-config.properties`
- `log4j2.xml`

## 📝 Documentation Gaps

The following areas could benefit from additional documentation:

1. **API Reference** - Missing Javadoc in ~100 methods
2. **User Manual** - Step-by-step usage guide
3. **Deployment Guide** - Production deployment instructions
4. **Testing Guide** - How to write and run tests
5. **Contributing Guide** - Code style and contribution process
6. **Troubleshooting** - Common issues and solutions

## 🛠️ Generating Documentation

### JavaDoc
```bash
mvn javadoc:javadoc
```
Output: `target/site/apidocs/index.html`

### Site Documentation (Full)
```bash
mvn site
```
Output: `target/site/index.html` (includes reports, JavaDoc, etc.)

## 📍 External Resources

### Official Game Information
- Official site: http://www.eternityii.com
- Fan site: http://www.tetravexii.com/
- Community: http://www.eternity.net/
- French community: http://www.eternity2.fr/
- Yahoo group: http://games.groups.yahoo.com/group/eternity_two/

## 📋 Document Maintenance

### Last Updated
- README.md: Original
- ARCHITECTURE.md: 2025-11-23
- JavaDoc: 2025-11-23 (auto-generated)
- This file: 2025-11-23

### Maintenance Tasks
- [ ] Add missing JavaDoc comments (100 warnings)
- [ ] Create user manual
- [ ] Document deployment procedures
- [ ] Add troubleshooting guide
- [ ] Update README with recent features (WebSocket, splash screen)
