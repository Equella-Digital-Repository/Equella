package com.tle.core.guice;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.guicerecipes.support.internal.Errors;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.UntargettedBinding;

public class DependencyAnalyzer extends DefaultBindingTargetVisitor<Object, Void>
{
	private final Set<Key<?>> dependentKeys = new HashSet<Key<?>>();
	private final Set<TypeLiteral<?>> scannedTypes = new HashSet<TypeLiteral<?>>();
	private Errors errors;

	public DependencyAnalyzer()
	{
		errors = new Errors();
	}

	public void addDependency(Key<?> dependency)
	{
		dependentKeys.add(dependency);
	}

	@Override
	public Void visit(UntargettedBinding<? extends Object> untargettedBinding)
	{
		analyzeImplementation(untargettedBinding.getKey().getTypeLiteral(), false);
		return null;
	}

	@Override
	public Void visit(LinkedKeyBinding<? extends Object> linkedKeyBinding)
	{
		analyzeImplementation(linkedKeyBinding.getLinkedKey().getTypeLiteral(), false);
		return null;
	}

	public Set<Key<?>> getDependentKeys()
	{
		return dependentKeys;
	}

	@Override
	protected Void visitOther(Binding<?> binding)
	{
		if( binding instanceof HasDependencies )
		{
			analyzeDependencies(((HasDependencies) binding).getDependencies());
		}
		return null;
	}

	private void analyzeDependencies(final Collection<Dependency<?>> dependencies)
	{
		for( final Dependency<?> d : dependencies )
		{
			final Key<?> key = d.getKey();
			InjectionPoint injectionPoint = d.getInjectionPoint();
			if( injectionPoint != null && injectionPoint.isOptional() )
			{
				continue;
			}
			if( key.getAnnotationType() == Assisted.class )
			{
				continue;
			}
			TypeLiteral<?> typeLiteral = key.getTypeLiteral();
			Class<?> rawType = typeLiteral.getRawType();
			if( rawType == Injector.class )
			{
				continue;
			}
			if( rawType == MembersInjector.class )
			{
				Key<?> injectedKey = key
					.ofType(((ParameterizedType) typeLiteral.getType()).getActualTypeArguments()[0]);
				dependentKeys.add(injectedKey);
				analyzeImplementation(injectedKey.getTypeLiteral(), true);
			}
			else if( rawType == Provider.class )
			{
				dependentKeys.add(key.ofType(((ParameterizedType) typeLiteral.getType()).getActualTypeArguments()[0]));
			}
			else
			{
				dependentKeys.add(key);
			}
		}
	}

	private void analyzeImplementation(final TypeLiteral<?> type, boolean ignoreConstructor)
	{
		if( !scannedTypes.contains(type) )
		{
			try
			{
				if( (type.getRawType().getModifiers() & (Modifier.INTERFACE | Modifier.ABSTRACT)) == 0 )
				{
					analyzeInjectionPoints(InjectionPoint.forInstanceMethodsAndFields(type));
					if( !ignoreConstructor )
					{
						analyzeDependencies(InjectionPoint.forConstructorOf(type).getDependencies());
					}
				}
			}
			catch( ConfigurationException ce )
			{
				errors.merge(ce.getErrorMessages());
			}
			scannedTypes.add(type);
		}
	}

	public void analyzeInjectionPoints(final Set<InjectionPoint> points)
	{
		for( final InjectionPoint p : points )
		{
			analyzeDependencies(p.getDependencies());
		}
	}

	public Errors getErrors()
	{
		return errors;
	}

}
