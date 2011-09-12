Summary: Storm GridHTTPs server v. ${version}-${age}
Name: storm-gridhttps-server
Version: ${version}
Release: ${age}
License: ASL 2.0
Vendor: EMI
Packager: ETICS
Prefix: /
BuildArch: noarch
BuildRoot: %{_builddir}/%{name}-%{version}
AutoReqProv: yes
Source0: storm-gridhttps-server-${version}.tar.gz

%define debug_package %{nil}

%description
This package contains the VOMS Admin server application and configuration tools.

%prep

%setup -c

%build
  
%install

%clean

%files
%defattr(-,root,root)
%dir /etc/storm/
%dir /etc/storm/gridhttps-server/
/etc/storm/gridhttps-server/*
%dir /usr/share/java/storm-gridhttps-server/
/usr/share/java/storm-gridhttps-server/*
%dir /usr/share/doc/storm-gridhttps-server-1.0.1/
/usr/share/doc/storm-gridhttps-server-1.0.1/*
%dir /var/log/storm/

%pre
echo "executing the pre"
exit 0

%preun
echo "executing the preun"
exit 0

